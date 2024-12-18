package byzzbench.simulator.protocols.event_driven_hotstuff;

import byzzbench.simulator.LeaderBasedProtocolReplica;
import byzzbench.simulator.Scenario;
import byzzbench.simulator.protocols.event_driven_hotstuff.messages.*;
import byzzbench.simulator.state.SerializableLogEntry;
import byzzbench.simulator.state.TotalOrderCommitLog;
import byzzbench.simulator.transport.DefaultClientRequestPayload;
import byzzbench.simulator.transport.MessagePayload;
import lombok.Getter;
import org.apache.commons.lang3.tuple.Pair;

import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class EDHotStuffReplica extends LeaderBasedProtocolReplica {

    @Getter
    long lastVoteHeight;
    //@Getter
    //EDHSNode leafNode;
    @Getter
    EDHSNode lockedNode;
    @Getter
    EDHSNode execNode;
    //@Getter
    //QuorumCertificate highQC;

    @Getter
    HashMap<String, EDHSNode> hashNodeMap;
    @Getter
    HashMap<Long, LeaderViewState> leaderViewStateMap;

    @Getter
    Queue<ClientRequest> pendingRequests;
    @Getter
    HashSet<ClientRequest> commitedRequests;
    @Getter
    HashMap<ClientRequest, EDHSNode> requestNodeMap;

    @Getter
    PriorityQueue<GenericMessage> pendingProposals;
    @Getter
    ArrayList<String> replicaIds;
    @Getter
    ArrayList<String> debugLog;

    @Getter
    EDHSPacemaker pacemaker;

    @Getter
    HashMap<String, ArrayList<IncompleteMessage>> incompleteMessages;

    public void log(String message) {
        debugLog.add(message);
        System.out.println("Replica " + getId() + ": " + message);
    }

    public EDHotStuffReplica(String nodeId, Scenario scenario, ArrayList<String> replicaIds) {
        super(nodeId, scenario, new TotalOrderCommitLog());
        this.replicaIds = replicaIds;
    }

    public EDHSNode getNode(String nodeHash) {
        return hashNodeMap.get(nodeHash);
    }

    public LeaderViewState getLeaderViewState(long viewNumber) {
        if (!pacemaker.isLeader(viewNumber)) return null;
        if (!leaderViewStateMap.containsKey(viewNumber))
            leaderViewStateMap.put(viewNumber, new LeaderViewState(this, viewNumber));
        return leaderViewStateMap.get(viewNumber);
    }

    public LeaderViewState getLeaderViewState() {
        return getLeaderViewState(getViewNumber());
    }

    public void nextView() {
        long nextViewNumber = getViewNumber() + 1;
        String nextLeaderId = pacemaker.getLeaderId(nextViewNumber);
        setView(nextViewNumber, nextLeaderId);

        log("View change. This is view " + getViewNumber());
    }

    public int getMinValidVotes() {
        int n = replicaIds.size();
        int f = (int) Math.floor((double) (n - 1) / 3);
        return n - f;
    }

    @Override
    public void initialize() {
        debugLog = new ArrayList<>();
        log("Initializing");

        leaderViewStateMap = new HashMap<>();
        hashNodeMap = new HashMap<>();
        pendingRequests = new LinkedList<>();
        requestNodeMap = new HashMap<>();
        commitedRequests = new HashSet<>();

        Comparator<GenericMessage> messageComparator = (m1, m2) -> (int) (m1.getViewNumber() - m2.getViewNumber());
        pendingProposals = new PriorityQueue<>(messageComparator);

        try {
            EDHSQuorumCertificate genesisQC0 = new EDHSQuorumCertificate("GENESIS", new QuorumSignature(new HashSet<>()));
            EDHSNode genesisNode0 = new EDHSNode("GENESIS", new ClientRequest("GENESIS", "GENESIS", "GENESIS"), genesisQC0, -3);
            hashNodeMap.put(genesisNode0.getHash(), genesisNode0);
            System.out.println("GENESIS 0 HASH: " + genesisNode0.getHash());

            EDHSQuorumCertificate genesisQC1 = new EDHSQuorumCertificate(genesisNode0.getHash(), new QuorumSignature(new HashSet<>()));
            EDHSNode genesisNode1 = new EDHSNode(genesisNode0.getHash(), new ClientRequest("GENESIS", "GENESIS", "GENESIS"), genesisQC1, -2);
            hashNodeMap.put(genesisNode1.getHash(), genesisNode1);
            System.out.println("GENESIS 1 HASH: " + genesisNode1.getHash());

            EDHSQuorumCertificate genesisQC2 = new EDHSQuorumCertificate(genesisNode1.getHash(), new QuorumSignature(new HashSet<>()));
            EDHSNode genesisNode2 = new EDHSNode(genesisNode1.getHash(), new ClientRequest("GENESIS", "GENESIS", "GENESIS"), genesisQC2, -1);
            hashNodeMap.put(genesisNode2.getHash(), genesisNode2);
            System.out.println("GENESIS 2 HASH: " + genesisNode2.getHash());

            EDHSQuorumCertificate genesisQC3 = new EDHSQuorumCertificate(genesisNode2.getHash(), new QuorumSignature(new HashSet<>()));
            EDHSNode genesisNode3 = new EDHSNode(genesisNode2.getHash(), new ClientRequest("GENESIS", "GENESIS", "GENESIS"), genesisQC3, 0);
            hashNodeMap.put(genesisNode3.getHash(), genesisNode3);
            System.out.println("GENESIS 3 HASH: " + genesisNode3.getHash());

            EDHSQuorumCertificate genesisQC4 = new EDHSQuorumCertificate(genesisNode3.getHash(), new QuorumSignature(new HashSet<>()));

            execNode = genesisNode1;
            lockedNode = genesisNode2;
            //leafNode = genesisNode3;
            //highQC = genesisQC4;
            this.pacemaker = new EDHSPacemaker(this, genesisNode3, genesisQC4);

            lastVoteHeight = 0;

            nextView();

            LeaderViewState firstLeaderState = getLeaderViewState();
            if (firstLeaderState != null) {
                //firstLeaderState.setPreviousNode(genesisNode3);
                replicaIds.forEach(rId -> firstLeaderState.addSignature(new PartialSignature(rId, genesisNode3.getHash())));
            }

        } catch (NoSuchAlgorithmException ignored) {

        }
    }

    @Override
    public void handleClientRequest(String clientId, Serializable request) throws Exception {
        if (request instanceof ClientRequest) {
            log("Received client request");

            pendingRequests.add((ClientRequest) request);
            pacemaker.onClientRequest();
        }

    }

    @Override
    public void handleMessage(String sender, MessagePayload message) throws Exception {
        switch (message) {
            case DefaultClientRequestPayload clientRequest -> handleClientRequest(sender, clientRequest.getOperation());
            case NewViewMessage newViewMessage -> onReceiveNewView(newViewMessage, sender);
            case GenericVote genericVote -> onReceiveVote(genericVote, sender);
            case GenericMessage genericMessage -> rememberProposal(genericMessage, sender);
            case AskMessage askMessage -> provideNode(askMessage, sender);
            case CatchUpMessage catchUpMessage -> catchUp(catchUpMessage, sender);
            default -> throw new IllegalStateException("Message type not supported.");
        }
    }

    // NEW-VIEW
    private void onReceiveNewView(NewViewMessage newViewMessage, String senderId) {
        if(isQCNodeMissing(newViewMessage, senderId)) return;

        log("Received NEW-VIEW message");
        pacemaker.onReceiveNewView(newViewMessage);

        long newViewNumber = newViewMessage.getViewNumber();
        LeaderViewState leaderViewState = getLeaderViewState(newViewNumber);
        if(leaderViewState != null) leaderViewState.addNewViewMessage(newViewMessage);
    }

    // GENERIC
    private void rememberProposal(GenericMessage proposal, String senderId) {
        if(isQCNodeMissing(proposal, senderId)) return;

        long proposalView = proposal.getViewNumber();

        log("Received GENERIC message for view " + proposalView + ", current view is " + getViewNumber());

        if (proposalView == getViewNumber()) {
            onReceiveProposal(proposal);
            while (pendingProposals.peek() != null && pendingProposals.peek().getViewNumber() <= getViewNumber()) {
                GenericMessage nextProposal = pendingProposals.poll();
                if (nextProposal.getViewNumber() == getViewNumber()) onReceiveProposal(nextProposal);
            }
        } else if (proposalView > getViewNumber()) {
            log("Saving proposal for a future view");
            pendingProposals.add(proposal);
        }

    }

    public boolean isProposalValid(GenericMessage proposal) {
        EDHSNode proposedNode = proposal.getNode();

        if(proposedNode.getHeight() != proposal.getViewNumber()) return false;

        return true;
    }

    private void onReceiveProposal(GenericMessage proposal) {
        EDHSNode proposedNode = proposal.getNode();
        hashNodeMap.put(proposedNode.getHash(), proposedNode);

        // validate proposal
        // TODO make sure proposal is valid
        if(!isProposalValid(proposal)) return;

        // viewChange
        nextView();

        // Remember client request to avoid duplicates
        ClientRequest clientRequest = proposedNode.getClientRequest();
        log("Processing proposal with height " + proposedNode.getHeight() + " for request " + clientRequest.getRequestId());
        if (commitedRequests.contains(clientRequest)) {
            log("Received proposal of already commited request. RequestId: " + clientRequest.getRequestId());
            return;
        }
        if (requestNodeMap.containsKey(clientRequest)) {
            EDHSNode previousRequestNode = requestNodeMap.get(clientRequest);
            if (!previousRequestNode.equals(proposedNode) && previousRequestNode.getHeight() > execNode.getHeight()) {
                log("Received proposal of currently pending request. RequestId: " + clientRequest.getRequestId());
                return;
            }
        }
        requestNodeMap.put(clientRequest, proposedNode);

        // vote
        EDHSNode node2 = hashNodeMap.get(proposedNode.getJustify().getNodeHash());
        if ((proposedNode.getHeight() > lastVoteHeight) && ((proposedNode.isExtensionOf(lockedNode, hashNodeMap)) || (node2.getHeight() > lockedNode.getHeight()))) {
            lastVoteHeight = proposedNode.getHeight();
            long voteView = proposal.getViewNumber() + 1;
            sendMessage(new GenericVote(voteView, proposedNode, getId()), pacemaker.getLeaderId(voteView));
            log("Sent vote for node " + proposedNode.getHash() + " with height " + proposedNode.getHeight());
        } else {
            log("Deciding NOT to vote for node " + proposedNode.getHash() + " with height " + proposedNode.getHeight());
        }

        // update
        update(proposedNode);
    }

    private void update(EDHSNode proposedNode) {
        EDHSNode node2 = hashNodeMap.get(proposedNode.getJustify().getNodeHash());
        EDHSNode node1 = hashNodeMap.get(node2.getJustify().getNodeHash());
        EDHSNode node0 = hashNodeMap.get(node1.getJustify().getNodeHash());

        // PRE-COMMIT
        pacemaker.updateHighQC(proposedNode.getJustify());
        // COMMIT
        if (node1.getHeight() > lockedNode.getHeight()) {
            lockedNode = node1;
            log("COMMIT. Locked node updated with node " + node1.getHash() + " with height " + node1.getHeight());
        }
        // DECIDE
        if (node2.isChildOf(node1) && node1.isChildOf(node0)) {
            onCommit(node0);
            execNode = node0;
            log("DECIDE. node " + node0.getHash() + " with height " + node0.getHeight());
        }
    }

    private void onCommit(EDHSNode node) {
        if (execNode.getHeight() < node.getHeight()) {
            onCommit(hashNodeMap.get(node.getParentHash()));
            commitOperation(new SerializableLogEntry(node.getClientRequest()));
            commitedRequests.add(node.getClientRequest());
            log("Commiting node " + node.getHash() + " with height " + node.getHeight() + ", clientRequest " + node.getClientRequest().getRequestId());

            ClientRequest clientRequest = node.getClientRequest();
            if (pacemaker.isLeader() && !clientRequest.getClientId().equals("GENESIS")) {
                sendReplyToClient(clientRequest);
            }
        }
    }

    // GENERIC-VOTE
    private void onReceiveVote(GenericVote vote, String senderId) {
        if(isQCNodeMissing(vote, senderId)) return;

        log("Received vote for node " + vote.getNode().getHash());

        long voteView = vote.getViewNumber();
        LeaderViewState leaderViewState = getLeaderViewState(voteView);
        if (leaderViewState == null) {
            log("Not leader for view " + voteView + ", discarding vote");
            return;
        }

        leaderViewState.addSignature(vote.getPartialSignature());
    }

    public EDHSNode onPropose(EDHSNode leaf, ClientRequest clientRequest, EDHSQuorumCertificate highQC) throws NoSuchAlgorithmException {
        // leaf.getHeight() + 1 is replaced with getViewNumber() to create implicit dummy nodes.
        EDHSNode newNode = new EDHSNode(leaf.getHash(), clientRequest, highQC, getViewNumber());
        hashNodeMap.put(newNode.getHash(), newNode);
        requestNodeMap.put(clientRequest, newNode);
        broadcastToAllReplicas(new GenericMessage(getViewNumber(), newNode));
        return newNode;
    }

    // Catch up mechanism
    // only use for messages with QC
    public boolean isQCNodeMissing(AbstractMessage message, String senderId) {
        EDHSQuorumCertificate qc;
        switch (message) {
            case NewViewMessage newViewMessage -> qc = newViewMessage.getJustify();
            case GenericMessage genericMessage -> qc = genericMessage.getNode().getJustify();
            case GenericVote genericVote -> qc = genericVote.getNode().getJustify();
            case CatchUpMessage catchUpMessage -> qc = catchUpMessage.getNode().getJustify();
            default -> throw new IllegalStateException("isQCNodeMissing: Unsupported message type");
        }
        String nodeHash = qc.getNodeHash();
        if (hashNodeMap.containsKey(nodeHash)) return false;
        else {
            sendMessage(new AskMessage(getViewNumber(), nodeHash), senderId);
            if(!incompleteMessages.containsKey(nodeHash)) incompleteMessages.put(nodeHash, new ArrayList<>());
            ArrayList<IncompleteMessage> incompleteMessagesForNode = incompleteMessages.get(nodeHash);
            incompleteMessagesForNode.add(new IncompleteMessage(message, senderId));
            return true;
        }
    }

    public void provideNode(AskMessage askMessage, String senderId) {
        if(hashNodeMap.containsKey(askMessage.getNodeHash())) {
            EDHSNode requestedNode = hashNodeMap.get(askMessage.getNodeHash());
            sendMessage(new CatchUpMessage(getViewNumber(), requestedNode), senderId);
        }
    }

    public void catchUp(CatchUpMessage catchUpMessage, String senderId) {
        if(!incompleteMessages.containsKey(catchUpMessage.getNode().getHash())) return;
        if(isQCNodeMissing(catchUpMessage, senderId)) return;

        EDHSNode catchUpNode = catchUpMessage.getNode();
        if(hashNodeMap.containsKey(catchUpNode.getHash())) return;
        hashNodeMap.put(catchUpNode.getHash(), catchUpNode);

        requestNodeMap.put(catchUpNode.getClientRequest(), catchUpNode);
        update(catchUpNode);

        ArrayList<IncompleteMessage> incompleteMessagesForNode = incompleteMessages.get(catchUpNode.getHash());
        incompleteMessagesForNode.forEach(im -> {
            switch (im.getMessage()) {
                case NewViewMessage m -> onReceiveNewView(m, im.getSenderId());
                case GenericMessage m -> rememberProposal(m, im.getSenderId());
                case GenericVote m -> onReceiveVote(m, im.getSenderId());
                case CatchUpMessage m -> catchUp(m, im.getSenderId());
                default -> throw new IllegalStateException("catchUp: Unsupported message type");
            }
        });
        incompleteMessages.remove(catchUpNode.getHash());
    }

    public void sendReplyToClient(ClientRequest clientRequest) {
        String requestId = clientRequest.getRequestId();
        ClientReply clientReply = new ClientReply(getId(), requestId, "Reply to: " + requestId);
        sendReplyToClient(clientRequest.getClientId(), clientReply);

        log("Sent reply (" + requestId + ") to client " + clientRequest.getClientId());
    }

    public ClientRequest nextClientRequest() {
        ClientRequest clientRequest = pendingRequests.poll();
        if (clientRequest == null) return null;

        if (commitedRequests.contains(clientRequest)) {
            log("Pending client request " + clientRequest.getRequestId() + " already commited.");
            clientRequest = nextClientRequest();
        } else if (requestNodeMap.containsKey(clientRequest)) {
            EDHSNode previousRequestNode = requestNodeMap.get(clientRequest);
            if (previousRequestNode.getHeight() > execNode.getHeight()) {
                log("Pending client request " + clientRequest.getRequestId() + " already has existing proposal.");
                clientRequest = nextClientRequest();
            }
        }

        return clientRequest;
    }

    private void broadcastToAllReplicas(AbstractMessage message) {
        multicastMessage(message, new TreeSet<>(replicaIds));
    }

    private void broadcastToOtherReplicas(AbstractMessage message) {
        SortedSet<String> otherReplicas = new TreeSet<>(replicaIds);
        otherReplicas.remove(getId());
        multicastMessage(message, otherReplicas);
    }
}
