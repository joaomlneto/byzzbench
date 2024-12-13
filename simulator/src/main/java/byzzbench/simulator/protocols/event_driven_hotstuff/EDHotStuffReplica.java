package byzzbench.simulator.protocols.event_driven_hotstuff;

import byzzbench.simulator.LeaderBasedProtocolReplica;
import byzzbench.simulator.Scenario;
import byzzbench.simulator.protocols.event_driven_hotstuff.messages.AbstractMessage;
import byzzbench.simulator.protocols.event_driven_hotstuff.messages.GenericMessage;
import byzzbench.simulator.protocols.event_driven_hotstuff.messages.GenericVote;
import byzzbench.simulator.protocols.event_driven_hotstuff.messages.NewViewMessage;
import byzzbench.simulator.state.SerializableLogEntry;
import byzzbench.simulator.state.TotalOrderCommitLog;
import byzzbench.simulator.transport.DefaultClientRequestPayload;
import byzzbench.simulator.transport.MessagePayload;
import lombok.Getter;

import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class EDHotStuffReplica extends LeaderBasedProtocolReplica {

    @Getter
    int lastVoteHeight;
    @Getter
    EDHSNode leafNode;
    @Getter
    EDHSNode lockedNode;
    @Getter
    EDHSNode execNode;
    @Getter
    QuorumCertificate highQC;

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

    public void log(String message) {
        debugLog.add(message);
        System.out.println("Replica " + getId() + ": " + message);
    }

    public EDHotStuffReplica(String nodeId, Scenario scenario, ArrayList<String> replicaIds) {
        super(nodeId, scenario, new TotalOrderCommitLog());
        this.replicaIds = replicaIds;
    }

    public boolean isLeader(long viewNumber) {
        String leaderId = getLeaderId(viewNumber);
        return getId().equals(leaderId);
    }

    public boolean isLeader() {
        return isLeader(getViewNumber());
    }

    public String getLeaderId(long viewNumber) {
        int leaderIndex = (int) (viewNumber % (replicaIds.size()));

        return replicaIds.get(leaderIndex);
    }

    public Integer getHighQCHeight() {
        EDHSNode node = hashNodeMap.get(highQC.getNodeHash());
        if (node == null) return Integer.MIN_VALUE;
        return node.getHeight();
    }

    public LeaderViewState getLeaderViewState() {
        return getLeaderViewState(getViewNumber());
    }

    public LeaderViewState getLeaderViewState(long viewNumber) {
        if (!isLeader(viewNumber)) return null;
        if (!leaderViewStateMap.containsKey(viewNumber))
            leaderViewStateMap.put(viewNumber, new LeaderViewState(this, viewNumber));
        return leaderViewStateMap.get(viewNumber);
    }

    public void nextView() {
        //clearAllTimeouts();

        long nextViewNumber = getViewNumber() + 1;
        String nextLeaderId = getLeaderId(nextViewNumber);
        setView(nextViewNumber, nextLeaderId);

        sendMessage(new NewViewMessage(getViewNumber(), highQC), nextLeaderId);

        log("View change. This is view " + getViewNumber());
        //setTimeout(this::nextView, 30000);
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
            QuorumCertificate genesisQC0 = new QuorumCertificate("GENESIS", new QuorumSignature(new HashSet<>()));
            EDHSNode genesisNode0 = new EDHSNode("GENESIS", new ClientRequest("GENESIS", "GENESIS", "GENESIS"), genesisQC0, -3);
            hashNodeMap.put(genesisNode0.getHash(), genesisNode0);
            System.out.println("GENESIS 0 HASH: " + genesisNode0.getHash());

            QuorumCertificate genesisQC1 = new QuorumCertificate(genesisNode0.getHash(), new QuorumSignature(new HashSet<>()));
            EDHSNode genesisNode1 = new EDHSNode(genesisNode0.getHash(), new ClientRequest("GENESIS", "GENESIS", "GENESIS"), genesisQC1, -2);
            hashNodeMap.put(genesisNode1.getHash(), genesisNode1);
            System.out.println("GENESIS 1 HASH: " + genesisNode1.getHash());

            QuorumCertificate genesisQC2 = new QuorumCertificate(genesisNode1.getHash(), new QuorumSignature(new HashSet<>()));
            EDHSNode genesisNode2 = new EDHSNode(genesisNode1.getHash(), new ClientRequest("GENESIS", "GENESIS", "GENESIS"), genesisQC2, -1);
            hashNodeMap.put(genesisNode2.getHash(), genesisNode2);
            System.out.println("GENESIS 2 HASH: " + genesisNode2.getHash());

            QuorumCertificate genesisQC3 = new QuorumCertificate(genesisNode2.getHash(), new QuorumSignature(new HashSet<>()));
            EDHSNode genesisNode3 = new EDHSNode(genesisNode2.getHash(), new ClientRequest("GENESIS", "GENESIS", "GENESIS"), genesisQC3, 0);
            hashNodeMap.put(genesisNode3.getHash(), genesisNode3);
            System.out.println("GENESIS 3 HASH: " + genesisNode3.getHash());

            QuorumCertificate genesisQC4 = new QuorumCertificate(genesisNode3.getHash(), new QuorumSignature(new HashSet<>()));

            leafNode = genesisNode3;
            lockedNode = genesisNode2;
            execNode = genesisNode1;
            highQC = genesisQC4;
            lastVoteHeight = 0;

            nextView();

            LeaderViewState firstLeaderState = getLeaderViewState();
            if (firstLeaderState != null) {
                firstLeaderState.setPreviousNode(genesisNode3);
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
            onBeat();
        }

    }

    @Override
    public void handleMessage(String sender, MessagePayload message) throws Exception {
        switch (message) {
            case DefaultClientRequestPayload clientRequest -> handleClientRequest(sender, clientRequest.getOperation());
            case NewViewMessage newViewMessage -> onReceiveNewView(newViewMessage);
            case GenericVote genericVote -> onReceiveVote(genericVote);
            case GenericMessage genericMessage -> onReceiveProposal(genericMessage);
            default -> throw new IllegalStateException("Message type not supported.");
        }
    }

    // NEW-VIEW
    private void onReceiveNewView(NewViewMessage newViewMessage) {
        log("Received NEW-VIEW message");
        updateHighQC((newViewMessage).getJustify());
    }

    // GENERIC
    private void onReceiveProposal(GenericMessage proposal) {
        long proposalView = proposal.getViewNumber();

        log("Received GENERIC message for view " + proposalView + ", current view is " + getViewNumber());

        if (proposalView == getViewNumber()) {
            processProposal(proposal);
            while (pendingProposals.peek() != null && pendingProposals.peek().getViewNumber() <= getViewNumber()) {
                GenericMessage nextProposal = pendingProposals.poll();
                if (nextProposal.getViewNumber() == getViewNumber()) processProposal(nextProposal);
            }
        } else if (proposalView > getViewNumber()) {
            log("Saving proposal for a future view");
            pendingProposals.add(proposal);
        }

    }

    private void processProposal(GenericMessage proposal) {
        // viewChange
        nextView();

        EDHSNode proposedNode = proposal.getNode();
        hashNodeMap.put(proposedNode.getHash(), proposedNode);

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

        // update procedure
        EDHSNode node2 = hashNodeMap.get(proposedNode.getJustify().getNodeHash());
        EDHSNode node1 = hashNodeMap.get(node2.getJustify().getNodeHash());
        EDHSNode node0 = hashNodeMap.get(node1.getJustify().getNodeHash());
        //

        // vote
        if ((proposedNode.getHeight() > lastVoteHeight) && ((proposedNode.isExtensionOf(lockedNode, hashNodeMap)) || (node2.getHeight() > lockedNode.getHeight()))) {
            lastVoteHeight = proposedNode.getHeight();
            long voteView = proposal.getViewNumber() + 1;
            sendMessage(new GenericVote(voteView, proposedNode, getId()), getLeaderId(voteView));
            log("Sent vote for node " + proposedNode.getHash() + " with height " + proposedNode.getHeight());
        } else {
            log("Deciding NOT to vote for node " + proposedNode.getHash() + " with height " + proposedNode.getHeight());
        }

        // update procedure

        // PRE-COMMIT
        updateHighQC(proposedNode.getJustify());
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

        LeaderViewState leaderViewState = getLeaderViewState();
        if (leaderViewState != null) leaderViewState.setPreviousNode(proposedNode);

    }

    private void onCommit(EDHSNode node) {
        if (execNode.getHeight() < node.getHeight()) {
            onCommit(hashNodeMap.get(node.getParentHash()));
            commitOperation(new SerializableLogEntry(node.getClientRequest()));
            commitedRequests.add(node.getClientRequest());
            log("Commiting node " + node.getHash() + " with height " + node.getHeight() + ", clientRequest " + node.getClientRequest().getRequestId());

            ClientRequest clientRequest = node.getClientRequest();
            if (isLeader() && !clientRequest.getClientId().equals("GENESIS")) {
                sendReplyToClient(clientRequest);
            }
        }
    }

    public void sendReplyToClient(ClientRequest clientRequest) {
        String requestId = clientRequest.getRequestId();
        ClientReply clientReply = new ClientReply(getId(), requestId, "Reply to: " + requestId);
        sendReplyToClient(clientRequest.getClientId(), clientReply);

        log("Sent reply (" + requestId + ") to client " + clientRequest.getClientId());
    }

    // GENERIC-VOTE
    private void onReceiveVote(GenericVote vote) {
        log("Received vote for node " + vote.getNode().getHash());

        long voteView = vote.getViewNumber();
        LeaderViewState leaderViewState = getLeaderViewState(voteView);
        if (leaderViewState == null) {
            log("Not leader for view " + voteView + ", discarding vote");
            return;
        }

        leaderViewState.addSignature(vote.getPartialSignature());
    }

    public void updateHighQC(QuorumCertificate newQC) {
        if (newQC.equals(highQC)) return;

        EDHSNode newQCNode = hashNodeMap.get(newQC.getNodeHash());
        EDHSNode highQCNode = hashNodeMap.get(highQC.getNodeHash());

        if (newQCNode.getHeight() > highQCNode.getHeight()) {
            log("Updating highQC with QC for node " + newQCNode.getHash() + " with height " + newQCNode.getHeight());
            highQC = newQC;
            leafNode = newQCNode;
        }
    }

    private ClientRequest nextClientRequest() {
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

    public void onBeat() throws NoSuchAlgorithmException {
        LeaderViewState viewState = getLeaderViewState();
        if (viewState == null) {
            log("Cannot create proposal. Not leader for view " + getViewNumber());
            return;
        }

        if (viewState.isAbleToSend()) {
            ClientRequest clientRequest = nextClientRequest();
            if (clientRequest != null) {
                leafNode = proposeNext(leafNode, clientRequest, highQC);
                viewState.setViewActionDone();
                log("Created proposal with height " + leafNode.getHeight() + " for request " + clientRequest.getRequestId());
            } else log("Cannot create proposal. No known client requests.");
        } else log("Cannot create proposal. QC not ready.");
    }

    private EDHSNode proposeNext(EDHSNode leaf, ClientRequest clientRequest, QuorumCertificate highQC) throws NoSuchAlgorithmException {
        EDHSNode newNode = new EDHSNode(leaf.getHash(), clientRequest, highQC, leaf.getHeight() + 1);
        hashNodeMap.put(newNode.getHash(), newNode);
        requestNodeMap.put(clientRequest, newNode);
        broadcastToAllReplicas(new GenericMessage(getViewNumber(), newNode));
        return newNode;
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
