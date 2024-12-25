package byzzbench.simulator.protocols.event_driven_hotstuff;

import byzzbench.simulator.LeaderBasedProtocolReplica;
import byzzbench.simulator.Scenario;
import byzzbench.simulator.protocols.event_driven_hotstuff.messages.*;
import byzzbench.simulator.state.SerializableLogEntry;
import byzzbench.simulator.state.TotalOrderCommitLog;
import byzzbench.simulator.transport.DefaultClientRequestPayload;
import byzzbench.simulator.transport.MessagePayload;
import lombok.Getter;

import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
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
    HashSet<ClientRequest> pendingRequests;
    @Getter
    HashSet<ClientRequest> commitedRequests;

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

    @Getter
    int timeout;

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
        clearAllTimeouts();

        long nextViewNumber = getViewNumber() + 1;
        String nextLeaderId = pacemaker.getLeaderId(nextViewNumber);
        setView(nextViewNumber, nextLeaderId);

        LeaderViewState leaderViewState = getLeaderViewState();
        if(leaderViewState != null) leaderViewState.onViewStart();

        log("View change. This is view " + getViewNumber());

        timeout = timeout * 2;
        setTimeout("onNextSyncView timout",pacemaker::onNextSyncView, Duration.ofSeconds(timeout));
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

        timeout = 1;
        leaderViewStateMap = new HashMap<>();
        hashNodeMap = new HashMap<>();
        pendingRequests = new HashSet<>();
        commitedRequests = new HashSet<>();
        incompleteMessages = new HashMap<>();

        Comparator<GenericMessage> messageComparator = (m1, m2) -> (int) (m1.getViewNumber() - m2.getViewNumber());
        pendingProposals = new PriorityQueue<>(messageComparator);

        try {
            EDHSQuorumCertificate genesisQC0 = new EDHSQuorumCertificate("GENESIS", new QuorumSignature(new HashSet<>()));
            EDHSNode genesisNode0 = new EDHSNode("GENESIS", new ClientRequest("GENESIS", "GENESIS", "GENESIS"), genesisQC0, -4);
            hashNodeMap.put(genesisNode0.getHash(), genesisNode0);
            System.out.println("GENESIS 0 HASH: " + genesisNode0.getHash());

            EDHSQuorumCertificate genesisQC1 = new EDHSQuorumCertificate(genesisNode0.getHash(), new QuorumSignature(new HashSet<>()));
            EDHSNode genesisNode1 = new EDHSNode(genesisNode0.getHash(), new ClientRequest("GENESIS", "GENESIS", "GENESIS"), genesisQC1, -3);
            hashNodeMap.put(genesisNode1.getHash(), genesisNode1);
            System.out.println("GENESIS 1 HASH: " + genesisNode1.getHash());

            EDHSQuorumCertificate genesisQC2 = new EDHSQuorumCertificate(genesisNode1.getHash(), new QuorumSignature(new HashSet<>()));
            EDHSNode genesisNode2 = new EDHSNode(genesisNode1.getHash(), new ClientRequest("GENESIS", "GENESIS", "GENESIS"), genesisQC2, -2);
            hashNodeMap.put(genesisNode2.getHash(), genesisNode2);
            System.out.println("GENESIS 2 HASH: " + genesisNode2.getHash());

            EDHSQuorumCertificate genesisQC3 = new EDHSQuorumCertificate(genesisNode2.getHash(), new QuorumSignature(new HashSet<>()));
            EDHSNode genesisNode3 = new EDHSNode(genesisNode2.getHash(), new ClientRequest("GENESIS", "GENESIS", "GENESIS"), genesisQC3, -1);
            hashNodeMap.put(genesisNode3.getHash(), genesisNode3);
            System.out.println("GENESIS 3 HASH: " + genesisNode3.getHash());

            HashSet<PartialSignature> QC4Signatures = new HashSet<>(replicaIds.stream().map(rId -> new PartialSignature(rId, genesisNode3.getHash())).toList());
            EDHSQuorumCertificate genesisQC4 = new EDHSQuorumCertificate(genesisNode3.getHash(), new QuorumSignature(QC4Signatures));

            execNode = genesisNode1;
            lockedNode = genesisNode2;
            //leafNode = genesisNode3;
            //highQC = genesisQC4;
            this.pacemaker = new EDHSPacemaker(this, genesisNode3, genesisQC4);

            lastVoteHeight = -1;

            nextView();

            LeaderViewState firstLeaderState = getLeaderViewState();
            if (firstLeaderState != null) {
                //firstLeaderState.setPreviousNode(genesisNode3);
                replicaIds.forEach(rId -> firstLeaderState.addVote(new GenericVote(0, genesisNode3, rId)));
            }

        } catch (NoSuchAlgorithmException ignored) {

        }
    }

    @Override
    public void handleClientRequest(String clientId, Serializable request) throws Exception {
        synchronized(this) {
            if (request instanceof ClientRequest) {
                log("Received client request");

                pendingRequests.add((ClientRequest) request);
                pacemaker.onClientRequest();
            }
        }
    }

    @Override
    public void handleMessage(String sender, MessagePayload message) throws Exception {
        synchronized(this) {
            switch (message) {
                case DefaultClientRequestPayload clientRequest ->
                        handleClientRequest(sender, clientRequest.getOperation());
                case NewViewMessage newViewMessage -> onReceiveNewView(newViewMessage, sender);
                case GenericVote genericVote -> onReceiveVote(genericVote, sender);
                case GenericMessage genericMessage -> rememberProposal(genericMessage, sender);
                case AskMessage askMessage -> onAskMessage(askMessage, sender);
                case TellMessage tellMessage -> onTellMessage(tellMessage, sender);
                default -> throw new IllegalStateException("Message type not supported.");
            }
        }
    }

    // NEW-VIEW
    private void onReceiveNewView(NewViewMessage newViewMessage, String senderId) {
        log("Received NEW-VIEW message from " + senderId);
        if(!isNewViewValid(newViewMessage)) return;
        if(isQCNodeMissing(newViewMessage, senderId)) return;

        pacemaker.onReceiveNewView(newViewMessage);

        long newViewNumber = newViewMessage.getViewNumber();
        LeaderViewState leaderViewState = getLeaderViewState(newViewNumber);
        if(leaderViewState != null) leaderViewState.addNewViewMessage(newViewMessage, senderId);
        else log("Discarding NEW-VIEW message. Not loeader for view " + newViewNumber);
    }

    // GENERIC
    private void rememberProposal(GenericMessage proposal, String senderId) {
        long proposalView = proposal.getViewNumber();
        log("Received GENERIC message from " + senderId + " for view " + proposalView + ", current view is " + getViewNumber());

        if(!isProposalValid(proposal, senderId)) return;
        if(isQCNodeMissing(proposal, senderId)) return;

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

    private void onReceiveProposal(GenericMessage proposal) {
        EDHSNode proposedNode = proposal.getNode();
        hashNodeMap.put(proposedNode.getHash(), proposedNode);

        // Remember client request to avoid duplicates
        ClientRequest clientRequest = proposedNode.getClientRequest();
        log("Processing proposal with height " + proposedNode.getHeight() + " for request " + clientRequest.getRequestId());
        if (commitedRequests.contains(clientRequest)) {
            log("Received proposal of already commited request. RequestId: " + clientRequest.getRequestId());
            return;
        }
        EDHSNode node2 = hashNodeMap.get(proposedNode.getJustify().getNodeHash());
        EDHSNode node1 = hashNodeMap.get(node2.getJustify().getNodeHash());
        EDHSNode node0 = hashNodeMap.get(node1.getJustify().getNodeHash());
        if(node2.getClientRequest().equals(clientRequest) && proposedNode.isChildOf(node2)) return;
        if(node1.getClientRequest().equals(clientRequest) && node2.isChildOf(node1) && proposedNode.isChildOf(node2)) return;
        if(node0.getClientRequest().equals(clientRequest) && node1.isChildOf(node0) && node2.isChildOf(node1)) return;

        // vote
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

        // viewChange
        nextView();

        processIncompleteMessages(proposedNode);
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
            if(onCommit(node0)) execNode = node0;
            log("DECIDE. node " + node0.getHash() + " with height " + node0.getHeight());
        }
    }

    private boolean onCommit(EDHSNode node) {
        if (execNode.getHeight() < node.getHeight()) {
            onCommit(hashNodeMap.get(node.getParentHash()));
            commitOperation(new SerializableLogEntry(node.getClientRequest()));
            commitedRequests.add(node.getClientRequest());
            pendingRequests.remove(node.getClientRequest());
            log("Commiting node " + node.getHash() + " with height " + node.getHeight() + ", clientRequest " + node.getClientRequest().getRequestId());

            ClientRequest clientRequest = node.getClientRequest();
            if (!clientRequest.getClientId().equals("GENESIS")) {
                sendReplyToClient(clientRequest);
            }
            return true;
        }
        return false;
    }

    // GENERIC-VOTE
    private void onReceiveVote(GenericVote vote, String senderId) {
        log("Received vote for node " + vote.getNode().getHash() + " from " + senderId);

        if(!isVoteValid(vote, senderId)) return;
        if(isQCNodeMissing(vote, senderId)) return;

        long voteView = vote.getViewNumber();
        LeaderViewState leaderViewState = getLeaderViewState(voteView);
        if (leaderViewState == null) {
            log("Not leader for view " + voteView + ", discarding vote");
            return;
        }

        leaderViewState.addVote(vote);
    }

    public EDHSNode onPropose(EDHSNode leaf, ClientRequest clientRequest, EDHSQuorumCertificate highQC) throws NoSuchAlgorithmException {
        // leaf.getHeight() + 1 is replaced with getViewNumber() to create implicit dummy nodes.
        EDHSNode newNode = new EDHSNode(leaf.getHash(), clientRequest, highQC, getViewNumber());
        //hashNodeMap.put(newNode.getHash(), newNode);
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
            case TellMessage tellMessage -> qc = tellMessage.getNode().getJustify();
            default -> throw new IllegalStateException("isQCNodeMissing: Unsupported message type");
        }
        String nodeHash = qc.getNodeHash();
        if (hashNodeMap.containsKey(nodeHash)) return false;
        else {
            log("QC node is missing. Cannot process " + message.getType() + " message.");
            log("ASK for node " + nodeHash);
            sendMessage(new AskMessage(getViewNumber(), nodeHash), senderId);
            if(!incompleteMessages.containsKey(nodeHash)) incompleteMessages.put(nodeHash, new ArrayList<>());
            ArrayList<IncompleteMessage> incompleteMessagesForNode = incompleteMessages.get(nodeHash);
            incompleteMessagesForNode.add(new IncompleteMessage(message, senderId));
            return true;
        }
    }

    public void onAskMessage(AskMessage askMessage, String senderId) {
        log("Received ASK message for node " + askMessage.getNodeHash() + " from " + senderId);
        if(hashNodeMap.containsKey(askMessage.getNodeHash())) {
            EDHSNode requestedNode = hashNodeMap.get(askMessage.getNodeHash());
            sendMessage(new TellMessage(getViewNumber(), requestedNode), senderId);
            log("Sent TELL message for node " + requestedNode.getHash() + " with height " + requestedNode.getHeight() + " to " + senderId);
        }
    }

    public void onTellMessage(TellMessage tellMessage, String senderId) {
        EDHSNode catchUpNode = tellMessage.getNode();
        log("Received TELL massage for node " + catchUpNode.getHash() + " with height " + catchUpNode.getHeight() + " from " + senderId);
        if(!incompleteMessages.containsKey(catchUpNode.getHash())) {
            log("Node is not missing. Discarding TELL message");
            return;
        }
        if(isQCNodeMissing(tellMessage, senderId)) return;

        catchUp(catchUpNode);
    }

    public void catchUp(EDHSNode missingNode) {
        if(hashNodeMap.containsKey(missingNode.getHash())) return;
        hashNodeMap.put(missingNode.getHash(), missingNode);

        update(missingNode);

        if(missingNode.getHeight() == getViewNumber()) nextView();
        processIncompleteMessages(missingNode);

        log("Caught up with node " + missingNode.getHash());
    }

    public void processIncompleteMessages(EDHSNode missingNode) {
        if(!incompleteMessages.containsKey(missingNode.getHash())) return;
        ArrayList<IncompleteMessage> incompleteMessagesForNode = incompleteMessages.get(missingNode.getHash());
        incompleteMessagesForNode.forEach(im -> {
            log("Processing incomplete message " + im.getMessage().getType());
            switch (im.getMessage()) {
                case NewViewMessage m -> onReceiveNewView(m, im.getSenderId());
                case GenericMessage m -> rememberProposal(m, im.getSenderId());
                case GenericVote m -> onReceiveVote(m, im.getSenderId());
                case TellMessage m -> onTellMessage(m, im.getSenderId());
                default -> throw new IllegalStateException("catchUp: Unsupported message type");
            }
        });
        incompleteMessages.remove(missingNode.getHash());
    }

    public void sendReplyToClient(ClientRequest clientRequest) {
        String requestId = clientRequest.getRequestId();
        ClientReply clientReply = new ClientReply(getId(), requestId, "Reply to: " + requestId);
        sendReplyToClient(clientRequest.getClientId(), clientReply);

        log("Sent reply (" + requestId + ") to client " + clientRequest.getClientId());
    }

    public ClientRequest nextClientRequest(EDHSNode leaf) {
        List<ClientRequest> requestlist = pendingRequests.stream().toList();
        for(ClientRequest clientRequest : requestlist) {
            if(leaf.getClientRequest().equals(clientRequest)) continue;
            EDHSNode node2 = hashNodeMap.get(leaf.getJustify().getNodeHash());
            if(node2.getClientRequest().equals(clientRequest) && leaf.isChildOf(node2)) continue;
            EDHSNode node1 = hashNodeMap.get(node2.getJustify().getNodeHash());
            if(node1.getClientRequest().equals(clientRequest) && node2.isChildOf(node1) && leaf.isChildOf(node2)) continue;

            return clientRequest;
        }
        return null;
    }

    private void broadcastToAllReplicas(AbstractMessage message) {
        multicastMessage(message, new TreeSet<>(replicaIds));
    }

    private void broadcastToOtherReplicas(AbstractMessage message) {
        SortedSet<String> otherReplicas = new TreeSet<>(replicaIds);
        otherReplicas.remove(getId());
        multicastMessage(message, otherReplicas);
    }

    public boolean isQCValid(EDHSQuorumCertificate qc) {
        return qc.isValid(getMinValidVotes());
    }

    public boolean isProposalValid(GenericMessage proposal, String senderId) {
        if(!pacemaker.getLeaderId(proposal.getViewNumber()).equals(senderId)) {
            log("Discarding proposal. Proposer is not a leader");
            return false;
        }

        EDHSNode proposedNode = proposal.getNode();
        if(proposedNode.getHeight() != proposal.getViewNumber()) {
            log("Discarding proposal. node height does not match proposal view number");
            return false;
        }
        if(!isQCValid(proposedNode.getJustify())) {
            log("Discarding proposal. QC is invalid");
            return false;
        }

        return true;
    }

    public boolean isVoteValid(GenericVote vote, String senderId) {
        PartialSignature ps = vote.getPartialSignature();
        EDHSNode node = vote.getNode();

        if(!ps.getSenderId().equals(senderId)) {
            log("Discarding vote. Not signed by sender");
            return false;
        }
        if(!ps.getProposedNodeHash().equals(node.getHash())) {
            log("Discarding vote. Signature references different node");
            return false;
        }
        if((node.getHeight() + 1) != vote.getViewNumber()) {
            log("Discarding vote. Node height does not match vote view number");
            return false;
        }

        return true;
    }

    public boolean isNewViewValid(NewViewMessage newViewMessage) {
        if(!isQCValid(newViewMessage.getJustify())) {
            log("Discarding newView. QC is not valid.");
            return false;
        };
        return true;
    }
}
