package byzzbench.simulator.protocols.XRPL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import byzzbench.simulator.Replica;
import byzzbench.simulator.protocols.XRPL.messages.XRPLProposeMessage;
import byzzbench.simulator.protocols.XRPL.messages.XRPLSubmitMessage;
import byzzbench.simulator.protocols.XRPL.messages.XRPLTxMessage;
import byzzbench.simulator.protocols.XRPL.messages.XRPLValidateMessage;
import byzzbench.simulator.state.TotalOrderCommitLog;
import byzzbench.simulator.transport.MessagePayload;
import byzzbench.simulator.transport.Transport;
import lombok.Getter;

@lombok.extern.java.Log
public class XRPLReplica extends Replica<XRPLLedger> {

    private @Getter Set<String> ourUNL;  //The nodeIDs of nodes in our UNL, our "peers"
    private @Getter List<String> pendingTransactions; //Our candidate set
    private @Getter XRPLReplicaState state; //this isn't visible in UI at first!
    private @Getter Map<String, XRPLProposal> currPeerProposals; 

    private @Getter XRPLLedger currWorkLedger; //this isn't visible in UI at first!
    private @Getter XRPLLedger prevLedger; //lastClosedLedger
    private @Getter XRPLLedger validLedger; //last fully validated ledger

    private @Getter long openTime;
    private @Getter long prevRoundTime;
    private @Getter double converge;
    
    private @Getter XRPLConsensusResult result; 

    private @Getter Map<String, XRPLLedger> validations; //map of last validated ledgers indexed on nodeId

    protected XRPLReplica(String nodeId, Set<String> nodeIds, Transport<XRPLLedger> transport, Set<String> UNL, XRPLLedger prevLedger_) {
        super(nodeId, nodeIds, transport, new TotalOrderCommitLog<>());
        this.ourUNL = UNL;
        this.result = new XRPLConsensusResult();
        this.state = null;  //set to open with first heartbeat
        
        //funky business
        this.prevRoundTime = 0;
        this.prevLedger = prevLedger_;
        this.pendingTransactions = new ArrayList<>();
        this.validations = new HashMap<>();
        this.validLedger = prevLedger_;
    }

    @Override
    public void initialize() {
        //nothing to do
    }

    @Override
    public void handleMessage(String sender, MessagePayload message) throws Exception {
        if (message instanceof XRPLProposeMessage propmsg) {
            proposeMessageHandler(propmsg);
            return;
        } else if (message instanceof XRPLSubmitMessage submsg) {
            submitMessageHandler(submsg);
            return;
        } else if (message instanceof XRPLValidateMessage valmsg) {
            validateMessageHandler(valmsg);
            return;
        } else if (message instanceof XRPLTxMessage txmsg) {
            recvTxHandler(txmsg);
            return;
        } else {
            throw new Exception("Unknown message type");
        }

    }

    private void recvTxHandler(XRPLTxMessage txmsg) {
        XRPLSubmitMessage submsg = new XRPLSubmitMessage(txmsg.getTx());
        this.broadcastMessageIncludingSelf(submsg);
    }

    private void submitMessageHandler(XRPLSubmitMessage msg) {
        try {
            String tx = msg.getTx();
            this.pendingTransactions.add(tx);
        } catch (Exception e) {
            log.info("Couldn't handle submit message in node " + this.getNodeId() + ": " + e.getMessage());
        }
    }

    private void validateMessageHandler(XRPLValidateMessage msg) {
        try {
            if (msg.getLedger().getSeq() == this.currWorkLedger.getSeq() /*TODO && verify signature */) {
                //TODO add msg.ledger to tree
                this.validations.put(msg.getSenderNodeId(), msg.getLedger());
                int valCount = 0;
                for (String nodeId : ourUNL) {
                    XRPLLedger ledger = validations.get(nodeId);
                    if (!(ledger == null)) {
                        if (ledger.equals(this.currWorkLedger)) {
                            valCount += 1;
                        }
                    }
                }
                if (valCount >= (0.8 * this.ourUNL.size()) && (this.currWorkLedger.getSeq() > this.validLedger.getSeq())) {
                    this.validLedger = this.currWorkLedger;
                    for (String tx : this.validLedger.transactions) {
                        log.info("Node " + this.getNodeId() + " should not have tx " + tx + " anymore!");
                        this.pendingTransactions.remove(tx);
                    }
                    //Exeucute txes

                }
            }
        } catch (Exception e) {
            log.info("Couldn't handle validate message in node " + this.getNodeId() + ": " + e.getMessage());
        }
    }

    private void proposeMessageHandler(XRPLProposeMessage msg) {
        try {
            XRPLProposal prop = msg.getProposal();
            if (prop.getPrevLedgerId().equals(this.prevLedger.getId()) && ourUNL.contains(msg.getSenderId())) {
                this.currPeerProposals.put(msg.getSenderId(), prop);
            }
        } catch (Exception e) {
            log.info("Couldn't handle propose message in node " + this.getNodeId() + ": " + e.getMessage());
        }
        
    }

    public void onHeartbeat() {
        if (this.state == XRPLReplicaState.ACCEPT) {
            //Nothing to do if we are validating a ledger
            return;
        } 

        //TODO now_ = now timestamp THINK HOW TO REPRESENT TIME CALLS
        
        /*  TODO XRPLLedger tempL = getPreferredLedger(this.validLedger);
        if (this.prevLedger != tempL) {
            this.prevLedger = tempL;
            startConsensus();
        }
        */
        if (this.state == null) {
            startConsensus();
            Runnable r = new XRPLHeartbeatRunnable(this);
            this.setTimeout(r, 5000);
            return;
        } //remove this once above is uncommented
        switch (this.state) {
            case XRPLReplicaState.OPEN:
                if (System.currentTimeMillis() - this.openTime >= (this.prevRoundTime / 2)) {
                    this.state = XRPLReplicaState.ESTABLISH;
                    closeLedger();
                }
                break;
            case XRPLReplicaState.ESTABLISH:
                //TODO result.roundTime = now - result.roundTime
                // converge = result.roundTime / max(prevRoundTime, 5s)
                UpdateOurProposals();
                if (checkConsensus()) {
                    this.state = XRPLReplicaState.ACCEPT;
                    handleAccept();
                }
                break;
            default:
                break;

            
        }
        //TODO reset timer 
        Runnable r = new XRPLHeartbeatRunnable(this);
        this.setTimeout(r, 5000);
        
    }

    private void UpdateOurProposals() {
        /* for (String nodeId : ourUNL) {
            //TODO remove stale proposals
            if (clock.now() - this.currPeerProposals.get(nodeId).time >= 20) {
                this.currPeerProposals.put(nodeId, null);
            } 
        } */
        boolean hasResChanged = false;
        for (DisputedTx dt : this.result.getDisputedTxs()) {
            if (updateVote(dt)) {
                hasResChanged = true;
                dt.switchOurVote();
                if (dt.getOurVote()) {
                    this.result.addTx(dt.getTx());
                } else {
                    this.result.removeTx(dt.getTx());
                }
            }
        }
        if (hasResChanged) {
            XRPLProposal prop = new XRPLProposal(this.prevLedger.getId(), this.result.getProposal().getSeq() + 1, this.result.getTxList(), this.getNodeId(), 1 /*TODO this should be now (or prev prop time) */);
            this.result.setProposal(prop);
            XRPLProposeMessage propmsg = new XRPLProposeMessage(prop, this.getNodeId());
            this.broadcastMessage(propmsg);
            this.result.resetDisputes();
            for (String nodeId : this.ourUNL) {
                if (currPeerProposals.get(nodeId) != null) {
                    createDisputes(currPeerProposals.get(nodeId).getTxns());
                }
            }
        }

    }

    private boolean updateVote(DisputedTx dt) {
        double threshold;
        if (this.converge < 0.5) {
            threshold = 0.5;
        } else if (this.converge < 0.85) {
            threshold = 0.65;
        } else if (this.converge < 2) {
            threshold = 0.7;
        } else {
            threshold = 0.95;
        }

        boolean newVote = (dt.getYesVotes() + boolToInt(dt.getOurVote())) / (dt.getNoVotes() + dt.getYesVotes() + 1) > threshold;
        return newVote != dt.getOurVote();
    }

    private int boolToInt(boolean b) {
        if (b) {
            return 1;
        } else {
            return 0;
        }
    }

    private void handleAccept() {
        XRPLLedger tmpL = new XRPLLedger(Integer.toString(Integer.parseInt(this.prevLedger.getId()) + 1), this.prevLedger.getId(), this.prevLedger.getSeq() + 1);
        tmpL.applyTxes(pendingTransactions);
        if (this.validations == null) {
            this.validations = new HashMap<>();
        }
        this.validations.put(this.getNodeId(), tmpL);
        //TODO sign the ledger
        XRPLValidateMessage val = new XRPLValidateMessage(this.getNodeId(), tmpL);
        this.prevLedger = tmpL;
        this.broadcastMessage(val);
        this.prevRoundTime = this.result.getRoundTime();
        this.startConsensus();
    }

    private boolean checkConsensus() {
        int agree = 0;
        int total = this.ourUNL.size();

        if (!this.ourUNL.contains(this.getNodeId())) {
            total += 1;
        }

        for ( Entry<String, XRPLProposal> entry : this.currPeerProposals.entrySet()) {
            if (entry.getValue().isTxListEqual(this.result.getTxList())) {
                agree += 1;
            } 
        }
        return (agree + 1) / total >= 0.8;
    }

    private void closeLedger() {
        this.currWorkLedger = new XRPLLedger(Integer.toString(Integer.parseInt(this.prevLedger.getId()) + 1), this.prevLedger.getId(), this.prevLedger.getSeq() + 1);
        this.currWorkLedger.applyTxes(this.pendingTransactions);
        this.result = new XRPLConsensusResult(this.pendingTransactions);
        XRPLProposal prop = new XRPLProposal(this.prevLedger.getId(), 0, this.result.getTxList(), getNodeId(), 1); // TODO get hash of prev ledger and call to clock.now for params
        // TODO this.result.setRoundTime(clock.now());
        // this.pendingTransactions.clear();
        this.result.setProposal(prop);

        XRPLProposeMessage propmsg = new XRPLProposeMessage(prop, this.getNodeId());
        this.broadcastMessage(propmsg);
        for (String nodeId : this.ourUNL) {
            if (currPeerProposals.get(nodeId) != null) {
                createDisputes(currPeerProposals.get(nodeId).getTxns());
            }
        }
    }

    private void createDisputes(List<String> txns) {
        Set<String> symmDiff = computeSymmetricDifference(this.result.getTxList(), txns);
        for (String tx : symmDiff) {
            DisputedTx dt = new DisputedTx(tx, this.result.containsTx(tx), 0, 0, new HashMap<>());
            for (String nodeId : this.ourUNL) {
                if (this.currPeerProposals.get(nodeId).containsTx(tx)) {
                    dt.incrementYesVotes();
                    dt.addEntryToVotesMap(nodeId, true);
                } else {
                    dt.incrementNoVotes();
                    dt.addEntryToVotesMap(nodeId, false);
                }
            }
            this.result.addDisputed(dt);
        }
    }

    private Set<String> computeSymmetricDifference(List<String> list1, List<String> list2) {
        Set<String> set1 = new HashSet<>(list1);
        Set<String> set2 = new HashSet<>(list2);
        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);
        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);
        union.removeAll(intersection);
        return union;
    }

    private XRPLLedger getPreferredLedger(XRPLLedger L) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getPreferredLedger'");
    }

    public void startConsensus() {
        this.state = XRPLReplicaState.OPEN;
        this.result.reset();
        this.converge = 0;
        //TODO this.openTime = clock.now();
        this.currPeerProposals = new HashMap<>();
    }


}
