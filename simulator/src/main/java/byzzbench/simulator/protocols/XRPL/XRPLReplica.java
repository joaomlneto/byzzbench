package byzzbench.simulator.protocols.XRPL;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
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
    private @Getter XRPLReplicaState state;
    private @Getter Map<String, XRPLProposal> currPeerProposals; 
    private @Getter Map<String, Deque<XRPLProposal>> recentPeerPositions; //List of recent proposals made by peers, used for playback

    private @Getter XRPLLedger currWorkLedger;
    private @Getter XRPLLedger prevLedger; //lastClosedLedger
    private @Getter XRPLLedger validLedger; //last fully validated ledger

    private @Getter long openTime;
    private @Getter long prevRoundTime;
    private @Getter double converge;
    
    private @Getter XRPLConsensusResult result; 

    private @Getter Map<String, XRPLLedger> validations; //map of last validated ledgers indexed on nodeId

    private @Getter XRPLLedgerTreeNode tree;

    protected XRPLReplica(String nodeId, Set<String> nodeIds, Transport<XRPLLedger> transport, Set<String> UNL, XRPLLedger prevLedger_) {
        super(nodeId, nodeIds, transport, new TotalOrderCommitLog<>());
        this.ourUNL = UNL;
        this.result = new XRPLConsensusResult();
        this.state = null;  //set to open with first heartbeat
        
        this.prevRoundTime = 0;
        this.prevLedger = prevLedger_;
        this.pendingTransactions = new ArrayList<>();
        this.validations = new HashMap<>();
        this.validLedger = prevLedger_;
        this.tree = new XRPLLedgerTreeNode(prevLedger_);

        this.recentPeerPositions = new HashMap<>();
        for (String nodeIdString : this.ourUNL) {
            this.recentPeerPositions.put(nodeIdString, new ArrayDeque<>());
        }
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

    /*
     * Transmit the recieved transaction through the gossip layer
     */
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
            if (msg.getLedger().getSeq() == this.currWorkLedger.getSeq() && msg.getLedger().isSignedBy(msg.getSenderNodeId())) {
                XRPLLedgerTreeNode n = new XRPLLedgerTreeNode(msg.getLedger());
                this.tree.addChild(n);
                this.validations.put(msg.getSenderNodeId(), msg.getLedger());

                //count validations for the recieved ledger
                int valCount = 0;
                for (String nodeId : ourUNL) {
                    XRPLLedger ledger = validations.get(nodeId);
                    if (!(ledger == null)) {
                        if (ledger.equals(this.currWorkLedger)) {
                            valCount += 1;
                        }
                    }
                }

                //if enough nodes validated, we can update validLedger
                if (valCount >= (0.8 * this.ourUNL.size()) && (this.currWorkLedger.getSeq() > this.validLedger.getSeq())) {
                    this.validLedger = this.currWorkLedger;
                    for (String tx : this.validLedger.transactions) {
                        for (String pendingtx : this.pendingTransactions) {
                            if (pendingtx.equals(tx)) {
                                this.pendingTransactions.remove(tx);
                            }
                        }
                    }
                    //Exeucute txes
                }
            }
        } catch (Exception e) {
            log.info("Couldn't handle validate message in node " + this.getNodeId() + ": " + e.getMessage());
        }
    }

    /*
     * Update our peer's position in our storage, make sure
     * the sequence number is not smaller than the one we already stored,
     * and it is working on the correct previous ledger.
     */
    private void proposeMessageHandler(XRPLProposeMessage msg) {
        try {
            XRPLProposal prop = msg.getProposal();
            Deque<XRPLProposal> props = this.recentPeerPositions.get(prop.getNodeId());
            if (props == null) {
                throw new RuntimeException("node not in our UNL");
            }
            if (props.size() >= 10) {
                props.removeFirst();
            }
            props.addLast(prop);
            if (prop.getPrevLedgerId().equals(this.prevLedger.getId()) && ourUNL.contains(msg.getSenderId())) {
                if (this.currPeerProposals.get(prop.getNodeId()) == null) {
                    this.currPeerProposals.put(msg.getSenderId(), prop);
                } else if (this.currPeerProposals.get(prop.getNodeId()).getSeq() < prop.getSeq()) {
                    this.currPeerProposals.put(msg.getSenderId(), prop);
                }
            }
        } catch (Exception e) {
            log.info("Error handling propose message in node " + this.getNodeId() + ": " + e.getMessage());
        }
        
    }

    public void onHeartbeat() {
        if (this.state == XRPLReplicaState.ACCEPT) {
            //Nothing to do if we are validating a ledger
            Runnable r = new XRPLHeartbeatRunnable(this);
            this.setTimeout(r, 5000);
            return;
        } 

        //TODO now_ = now timestamp THINK HOW TO REPRESENT TIME CALLS
        
        XRPLLedger tempL = getPreferredLedger(this.validLedger);
        if (!this.prevLedger.getId().equals(tempL.getId())) {
            log.info("found that the node: " + this.getNodeId() + " is not on the preferred ledger");
            this.prevLedger = tempL;
            startConsensus();
        }
        
        if (this.state == null) {
            startConsensus();
            Runnable r = new XRPLHeartbeatRunnable(this);
            this.setTimeout(r, 5000);
            return;
        }
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
        for (String nodeId : ourUNL) {
            if (this.currPeerProposals.get(nodeId) != null) {
                createDisputes(this.currPeerProposals.get(nodeId).getTxns());
            }
        }

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
            this.currWorkLedger = new XRPLLedger(this.currWorkLedger.getParentId(), this.currWorkLedger.getSeq(), this.result.getTxList());
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
        boolean newVote = (double) (dt.getYesVotes() + boolToInt(dt.getOurVote())) / (dt.getNoVotes() + dt.getYesVotes() + 1) > threshold;
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
        XRPLLedger tmpL = new XRPLLedger(this.prevLedger.getId(), this.prevLedger.getSeq() + 1, this.result.getTxList());
        if (this.validations == null) {
            this.validations = new HashMap<>();
        }
        tmpL.signLedger(this.getNodeId());
        this.validations.put(this.getNodeId(), tmpL);
        XRPLValidateMessage val = new XRPLValidateMessage(this.getNodeId(), tmpL);
        this.prevLedger = tmpL;
        this.broadcastMessage(val);
        this.prevRoundTime = this.result.getRoundTime();
        this.validateMessageHandler(val);
        this.startConsensus();
    }

    /*
     * Have we reached agreement in proposals with enough of our peers
     */
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
        return (double) (agree + 1) / total >= 0.8;
    }

    /*
     * "Close" the current working ledger, send a proposal to our peers.
     */
    private void closeLedger() {
        this.currWorkLedger = new XRPLLedger(this.prevLedger.getId(), this.prevLedger.getSeq() + 1, this.pendingTransactions);
        this.result = new XRPLConsensusResult(this.pendingTransactions);
        XRPLProposal prop = new XRPLProposal(this.prevLedger.getId(), 0, this.result.getTxList(), getNodeId(), 1); // TODO get hash of prev ledger and call to clock.now for params
        // TODO this.result.setRoundTime(clock.now());
        this.result.setProposal(prop);

        XRPLProposeMessage propmsg = new XRPLProposeMessage(prop, this.getNodeId());
        this.broadcastMessage(propmsg);
        for (String nodeId : this.ourUNL) {
            if (currPeerProposals.get(nodeId) != null) {
                createDisputes(currPeerProposals.get(nodeId).getTxns());
            }
        }
    }

    /*
     * Create disputes based on discrepencies on our proposal
     * and our peers'.
     */
    private void createDisputes(List<String> txns) {
        Set<String> symmDiff = computeSymmetricDifference(this.result.getTxList(), txns);
        for (String tx : symmDiff) {
            if (!this.result.disputesContain(tx)) {
                DisputedTx dt = new DisputedTx(tx, this.result.containsTx(tx), 0, 0, new HashMap<>());
                for (String nodeId : this.ourUNL) {
                    if (this.currPeerProposals.get(nodeId) != null) {
                        if (this.currPeerProposals.get(nodeId).containsTx(tx)) {
                            dt.incrementYesVotes();
                            dt.addEntryToVotesMap(nodeId, true);
                        } else {
                            dt.incrementNoVotes();
                            dt.addEntryToVotesMap(nodeId, false);
                        }
                    }
                }
                this.result.addDisputed(dt);
            }
            
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

    /*
     * Number of validations issued for the given ledger
     */
    private int tipSupport(String ledgerHash) {
        int count = 0;
        for (String nodeId : this.ourUNL) {
            if (this.validations.get(nodeId) != null) {
                if (this.validations.get(nodeId).getId().equals(ledgerHash)) {
                    count += 1;
                }
            }
        }
        return count;
    }

    private XRPLLedgerTreeNode findInTree(String ledgerHash, XRPLLedgerTreeNode n) {
        if (n.getLedger().getId().equals(ledgerHash)) {
            return n;
        } else {
            for (XRPLLedgerTreeNode tmp : n.getChildren()) {
                XRPLLedgerTreeNode tmp2 = findInTree(ledgerHash, tmp);
                if ( tmp2 != null) return tmp2;
            }
            return null;
        }
    }
    
    /*
     * Number of nodes that have not committed their validation 
     * for this sequence numbered ledger.
     */
    private int uncommitted(String ledgerHash) {
        XRPLLedgerTreeNode node = findInTree(ledgerHash, tree);
        int ret = 0;
        for (String nodeId : ourUNL) {
            if (this.validations.get(nodeId) != null) {
                if (this.validations.get(nodeId).getSeq() < node.getLedger().getSeq()) {
                    ret += 1;
                }
            }
        }
        return ret;
    }

    /*
     * The branch support for this ledger, i.e. the tip-support
     * for this ledger and its descendants.
     */
    private int support(String ledgerHash) {
        return supportHelper(findInTree(ledgerHash, tree));
    }

    private int supportHelper(XRPLLedgerTreeNode node) {
        if (node.getChildren().isEmpty()) {
            return tipSupport(node.getLedger().getId());
        } else {
            int ret = 0;
            for (XRPLLedgerTreeNode childNode : node.getChildren()) {
                ret += supportHelper(childNode);
            }
            return ret + tipSupport(node.getLedger().getId());
        }
    }

    private XRPLLedgerTreeNode getMaxSupportedChild(XRPLLedgerTreeNode node) {
        int max = Integer.MIN_VALUE;
        XRPLLedgerTreeNode ret = null;
        for (XRPLLedgerTreeNode child : node.getChildren()) {
            int curr = support(child.getLedger().getId());
            if ( curr > max) {
                ret = child;
                max = curr;
            }
        }
        return ret;
    }

    private XRPLLedgerTreeNode getSecondMaxSupportedChild(XRPLLedgerTreeNode node) {
        XRPLLedgerTreeNode actualMax = getMaxSupportedChild(node);
        int max = Integer.MIN_VALUE;
        XRPLLedgerTreeNode ret = null;
        for (XRPLLedgerTreeNode child : node.getChildren()) {
            if (!child.getLedger().getId().equals(actualMax.getLedger().getId())) {
                int curr = support(child.getLedger().getId());
                if ( curr > max) {
                    ret = child;
                    max = curr;
                }
            }
        }
        return ret;
    }

    private XRPLLedger getPreferredLedger(XRPLLedger L) {
        XRPLLedgerTreeNode node = findInTree(L.getId(), tree);

        if (node.getChildren().isEmpty()) {
            return node.getLedger();
        } else {
            XRPLLedgerTreeNode mNode = getMaxSupportedChild(node);

            if (uncommitted(mNode.getLedger().getId()) >= support(mNode.getLedger().getId())) {
                return L;
            } else if (getSecondMaxSupportedChild(node) != null && support(getSecondMaxSupportedChild(node).getLedger().getId()) + uncommitted(mNode.getLedger().getId()) < support(mNode.getLedger().getId())) {
                return getPreferredLedger(mNode.getLedger());
            } else {
                return L;
            }
        }
    }

    public void startConsensus() {
        this.state = XRPLReplicaState.OPEN;
        this.result.reset();
        this.converge = 0;
        //TODO this.openTime = clock.now();
        this.currPeerProposals = new HashMap<>();
        this.playbackProposals();
    }

    private void playbackProposals() {
        for (Entry<String, Deque<XRPLProposal>> propEntry : recentPeerPositions.entrySet()) {
            for (XRPLProposal proposal : propEntry.getValue()) {
                if (proposal.getPrevLedgerId().equals(this.prevLedger.getId())) {
                    if (ourUNL.contains(propEntry.getKey())) {
                        if (this.currPeerProposals.get(proposal.getNodeId()) == null) {
                            this.currPeerProposals.put(proposal.getNodeId(), proposal);
                        } else if (this.currPeerProposals.get(proposal.getNodeId()).getSeq() < proposal.getSeq()) {
                            this.currPeerProposals.put(proposal.getNodeId(), proposal);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void handleClientRequest(String clientId, Serializable request) throws Exception {
        String tx = request.toString();
        XRPLTxMessage txmsg = new XRPLTxMessage(tx, clientId);
        this.handleMessage(clientId, txmsg);
    }
}
