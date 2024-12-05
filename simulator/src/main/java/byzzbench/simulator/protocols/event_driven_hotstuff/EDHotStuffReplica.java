package byzzbench.simulator.protocols.event_driven_hotstuff;

import byzzbench.simulator.LeaderBasedProtocolReplica;
import byzzbench.simulator.protocols.event_driven_hotstuff.messages.AbstractMessage;
import byzzbench.simulator.protocols.event_driven_hotstuff.messages.GenericMessage;
import byzzbench.simulator.protocols.event_driven_hotstuff.messages.GenericVote;
import byzzbench.simulator.protocols.event_driven_hotstuff.messages.NewViewMessage;
import byzzbench.simulator.state.SerializableLogEntry;
import byzzbench.simulator.state.TotalOrderCommitLog;
import byzzbench.simulator.transport.MessagePayload;
import byzzbench.simulator.transport.Transport;
import lombok.Getter;

import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class EDHotStuffReplica extends LeaderBasedProtocolReplica {

    @Getter
    int vHeight;
    @Getter
    Node leafNode;
    @Getter
    Node lockedNode;
    @Getter
    Node execNode;
    @Getter
    QuorumCertificate highQC;

    @Getter
    HashMap<String, Node> hashNodeMap;
    @Getter
    HashMap<String, ArrayList<PartialSignature>> collectedSignaturesPerNode;
    List<String> replicaIds;

    public EDHotStuffReplica(String nodeId, SortedSet<String> nodeIds, Transport transport) {
        super(nodeId, nodeIds, transport, new TotalOrderCommitLog());
        replicaIds = nodeIds.stream().toList();
    }

    public boolean isLeader() {
        return getLeaderId().equals(getNodeId());
    }

    public void nextView() {
        //clearAllTimeouts();

        long nextViewNumber = getViewNumber() + 1;
        int nextLeaderIndex = (int) (nextViewNumber % (replicaIds.size() - 1));
        String nextLeaderId = replicaIds.get(nextLeaderIndex);
        setView(nextViewNumber, nextLeaderId);

        sendMessage(new NewViewMessage(highQC), nextLeaderId);

        //setTimeout(this::nextView, 30000);
    }

    public int getMinValidReplicas() {
        int n = replicaIds.size();
        int f = (int) Math.floor((double) (n - 1) / 3);
        return n - f;
    }

    @Override
    public void initialize() {
        System.out.println("Initializing EDHotStuff replica: " + this.getNodeId());

        collectedSignaturesPerNode = new HashMap<>();
        hashNodeMap = new HashMap<>();

        try {
            QuorumCertificate genesisQC0 = new QuorumCertificate("GENESIS", new QuorumSignature(new ArrayList<>()));
            Node genesisNode0 = new Node("GENESIS", new ClientCommand("GENESIS", "GENESIS"), genesisQC0, -3);
            hashNodeMap.put(genesisNode0.getHash(), genesisNode0);
            System.out.println("GENESIS 0 HASH: " + genesisNode0.getHash());

            QuorumCertificate genesisQC1 = new QuorumCertificate(genesisNode0.getHash(), new QuorumSignature(new ArrayList<>()));
            Node genesisNode1 = new Node(genesisNode0.getHash(), new ClientCommand("GENESIS", "GENESIS"), genesisQC1, -2);
            hashNodeMap.put(genesisNode1.getHash(), genesisNode1);
            System.out.println("GENESIS 1 HASH: " + genesisNode1.getHash());

            QuorumCertificate genesisQC2 = new QuorumCertificate(genesisNode1.getHash(), new QuorumSignature(new ArrayList<>()));
            Node genesisNode2 = new Node(genesisNode1.getHash(), new ClientCommand("GENESIS", "GENESIS"), genesisQC2, -1);
            hashNodeMap.put(genesisNode2.getHash(), genesisNode2);
            System.out.println("GENESIS 2 HASH: " + genesisNode2.getHash());

            QuorumCertificate genesisQC3 = new QuorumCertificate(genesisNode2.getHash(), new QuorumSignature(new ArrayList<>()));
            Node genesisNode3 = new Node(genesisNode2.getHash(), new ClientCommand("GENESIS", "GENESIS"), genesisQC3, 0);
            hashNodeMap.put(genesisNode3.getHash(), genesisNode3);
            System.out.println("GENESIS 3 HASH: " + genesisNode3.getHash());

            leafNode = genesisNode3;
            lockedNode = genesisNode3;
            execNode = genesisNode3;
            highQC = genesisQC3;
            vHeight = 0;

            nextView();
        } catch (NoSuchAlgorithmException ignored) {

        }
    }

    @Override
    public void handleClientRequest(String clientId, Serializable request) throws Exception {
        onBeat(new ClientCommand(clientId, request));
    }

    @Override
    public void handleMessage(String sender, MessagePayload message) throws Exception {
        AbstractMessage hotStuffMessage = (AbstractMessage) message;
        switch (hotStuffMessage.getMessageType()) {
            case NEW_VIEW -> onReceiveNewView((NewViewMessage) hotStuffMessage);
            case GENERIC -> onReceiveProposal((GenericMessage) hotStuffMessage);
            case GENERIC_VOTE -> onReceiveVote((GenericVote) hotStuffMessage);
        }
    }

    // NEW-VIEW
    private void onReceiveNewView(NewViewMessage newViewMessage) {
        updateHighQC((newViewMessage).getJustify());
    }

    // GENERIC
    private void onReceiveProposal(GenericMessage proposal) {
        Node proposedNode = proposal.getNode();
        hashNodeMap.put(proposedNode.getHash(), proposedNode);

        // update procedure
        Node node2 = hashNodeMap.get(proposedNode.getJustify().getNodeHash());
        Node node1 = hashNodeMap.get(node2.getJustify().getNodeHash());
        Node node0 = hashNodeMap.get(node1.getJustify().getNodeHash());
        //

        // viewChange
        nextView();

        // vote
        if ((proposedNode.getHeight() > vHeight) && ((proposedNode.isChildOf(lockedNode)) || (node2.getHeight() > lockedNode.getHeight()))) {
            vHeight = proposedNode.getHeight();
            sendMessage(new GenericVote(proposedNode, getNodeId()), getLeaderId());
        }

        // update procedure

        // PRE-COMMIT
        updateHighQC(proposedNode.getJustify());
        // COMMIT
        if (node1.getHeight() > lockedNode.getHeight()) lockedNode = node1;
        // DECIDE
        if (node2.isChildOf(node1) && node1.isChildOf(node0)) {
            onCommit(node0);
            execNode = node0;
        }
    }

    private void onCommit(Node node) {
        if (execNode.getHeight() < node.getHeight()) {
            onCommit(hashNodeMap.get(node.getParentHash()));
            commitOperation(new SerializableLogEntry(node.getCommand()));

            ClientCommand command = node.getCommand();
            if (!command.getClientID().equals("GENESIS"))
                sendReplyToClient(command.getClientID(), "Reply to: " + command.getCommand().toString());
        }
    }

    // GENERIC-VOTE
    private void onReceiveVote(GenericVote vote) {
        String nodeHash = vote.getNode().getHash();
        ArrayList<PartialSignature> voteSignatures = collectedSignaturesPerNode.computeIfAbsent(nodeHash, k -> new ArrayList<>());
        if (!voteSignatures.contains(vote.getPartialSignature())) voteSignatures.add(vote.getPartialSignature());
        if (voteSignatures.size() >= getMinValidReplicas()) {
            QuorumCertificate newQC = new QuorumCertificate(nodeHash, new QuorumSignature(voteSignatures));
            updateHighQC(newQC);
        }
    }

    private void updateHighQC(QuorumCertificate newQC) {
        if (newQC.equals(highQC)) return;

        Node newQCNode = hashNodeMap.get(newQC.getNodeHash());
        Node highQCNode = hashNodeMap.get(highQC.getNodeHash());

        if (newQCNode.getHeight() > highQCNode.getHeight()) {
            highQC = newQC;
            leafNode = newQCNode;
        }
    }

    private void onBeat(ClientCommand command) throws NoSuchAlgorithmException {
        if (isLeader()) leafNode = proposeNext(leafNode, command, highQC);
    }

    private Node proposeNext(Node leaf, ClientCommand command, QuorumCertificate highQC) throws NoSuchAlgorithmException {
        Node newNode = new Node(leaf.getHash(), command, highQC, leaf.getHeight() + 1);
        hashNodeMap.put(newNode.getHash(), newNode);
        broadcastMessageIncludingSelf(new GenericMessage(newNode));
        return newNode;
    }
}
