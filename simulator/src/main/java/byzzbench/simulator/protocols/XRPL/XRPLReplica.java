package byzzbench.simulator.protocols.XRPL;

import java.util.Set;

import byzzbench.simulator.Replica;
import byzzbench.simulator.protocols.XRPL.messages.XRPLProposeMessage;
import byzzbench.simulator.protocols.XRPL.messages.XRPLSubmitMessage;
import byzzbench.simulator.protocols.XRPL.messages.XRPLValidateMessage;
import byzzbench.simulator.state.CommitLog;
import byzzbench.simulator.state.TotalOrderCommitLog;
import byzzbench.simulator.transport.MessagePayload;
import byzzbench.simulator.transport.Transport;

public class XRPLReplica extends Replica<XRPLLedger> {

    private Set<String> ourUNL;  //The nodeIDs of nodes in our UNL, our "peers"
    private Set<Integer> pendingTransactions; //Pending transactions in our pool, represented by their IDs.
    private XRPLReplicaState state;
    private Set<XRPLProposal> currPeerProposals;

    private XRPLLedger currWorkLedger;
    private XRPLLedger prevLedger;
    private XRPLLedger validLedger;

    private long openTime;
    private long prevRoundTime;
    
    private XRPLConsensusResult result;

    protected XRPLReplica(String nodeId, Set<String> nodeIds, Transport<XRPLLedger> transport, Set<String> UNL) {
        super(nodeId, nodeIds, transport, new TotalOrderCommitLog<>());
        //TODO Auto-generated constructor stub
        this.ourUNL = UNL;
    }

    @Override
    public void initialize() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'initialize'");
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
        } else {
            throw new Exception("Uknown message type");
        }

    }

    private void submitMessageHandler(XRPLSubmitMessage msg) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'submitMessageHandler'");
    }

    private void validateMessageHandler(XRPLValidateMessage msg) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'validateMessageHandler'");
    }

    private void proposeMessageHandler(XRPLProposeMessage msg) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'proposeMessageHandler'");
    }

    public void sendTransaction(int ID) {
        if (this.state == XRPLReplicaState.OPEN) {
            this.pendingTransactions.add(ID);
        }
    }

    public void onHeartbeat() {
        if (this.state == XRPLReplicaState.ACCEPT) {
            //Nothing to do if we are validating a ledger
            return;
        } 

        //TODO now_ = now timestamp
        
        XRPLLedger tempL = getPreferredLedger(this.validLedger);
        if (this.prevLedger != tempL) {
            this.prevLedger = tempL;
            startConsensus(this.prevLedger);
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
                //UpdateOurProposals()
                if (checkConsensus()) {
                    this.state = XRPLReplicaState.ACCEPT;
                    handleAccept();
                }
                break;
        
            case XRPLReplicaState.ACCEPT:
                break;
            default:
                break;

            //TODO reset timer
        }
        
    }

    private void handleAccept() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'handleAccept'");
    }

    private boolean checkConsensus() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'checkConsensus'");
    }

    private void closeLedger() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'closeLedger'");
    }

    private XRPLLedger getPreferredLedger(XRPLLedger L) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getPreferredLedger'");
    }

    public void startConsensus(XRPLLedger L) {
        this.state = XRPLReplicaState.OPEN;
    }

    //TODO implement the data structures and algorithms in the analysis paper:

}
