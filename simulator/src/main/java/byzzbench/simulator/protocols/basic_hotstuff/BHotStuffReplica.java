package byzzbench.simulator.protocols.basic_hotstuff;

import byzzbench.simulator.LeaderBasedProtocolReplica;
import byzzbench.simulator.protocols.basic_hotstuff.messages.*;
import byzzbench.simulator.state.CommitLog;
import byzzbench.simulator.state.LogEntry;
import byzzbench.simulator.state.SerializableLogEntry;
import byzzbench.simulator.state.TotalOrderCommitLog;
import byzzbench.simulator.transport.MessagePayload;
import byzzbench.simulator.transport.Transport;
import org.apache.commons.lang3.NotImplementedException;

import java.io.Serializable;
import java.util.*;

public class BHotStuffReplica extends LeaderBasedProtocolReplica {
    private int viewNumber;
    private BHotStuffPhase currentPhase;

    private QuorumCertificate prepareQC;
    private QuorumCertificate lockedQC;
    private QuorumCertificate preCommitQC;
    private QuorumCertificate commitQC;

    private Node currentProposal;

    private ArrayList<NewViewMessage> newViewMessages;
    private ArrayList<PrepareVote> prepareVotes;
    private ArrayList<PreCommitVote> preCommitVotes;
    private ArrayList<CommitVote> commitVotes;

    protected BHotStuffReplica(String nodeId, SortedSet<String> nodeIds, Transport transport) {
        super(nodeId, nodeIds, transport, new TotalOrderCommitLog());
    }

    @Override
    public void initialize() {
        viewNumber = 1;
        prepareQC = null;
        lockedQC = null;
        currentPhase = BHotStuffPhase.PREPARE;
        newViewMessages = new ArrayList<>();
        prepareVotes = new ArrayList<>();
        preCommitVotes = new ArrayList<>();
        commitVotes = new ArrayList<>();
    }

    @Override
    public void handleClientRequest(String clientId, Serializable request) throws Exception {

    }

    @Override
    public void handleMessage(String sender, MessagePayload message) throws Exception {
        AbstractMessage hotStuffMessage = (AbstractMessage) message;
        switch (currentPhase) {
            case PREPARE -> handleMessagePrepare(sender, hotStuffMessage);
            case PRE_COMMIT -> handleMessagePreCommit(sender, hotStuffMessage);
            case COMMIT -> handleMessageCommit(sender, hotStuffMessage);
            case DECIDE -> handleMessageDecide(sender, hotStuffMessage);
        }
    }

    public void handleMessagePrepare(String sender, AbstractMessage message) {
        if (isLeader()) {
            if (message.matches(MessageType.NEW_VIEW, viewNumber - 1))
                newViewMessages.add((NewViewMessage) message);

            if (newViewMessages.size() >= getMinValidReplicas()) {
                var qcList = new ArrayList<>(newViewMessages.stream().map(NewViewMessage::getJustify).toList());
                qcList.sort(Comparator.comparingInt(QuorumCertificate::getViewNumber));
                var highQC = qcList.getLast();
                // TODO: Get the client's command from somewhere
                currentProposal = createLeaf(highQC.getNode(), "COMMAND");
                broadcastMessage(new PrepareMessage(viewNumber, highQC, currentProposal));
                newViewMessages.clear();
                currentPhase = BHotStuffPhase.PRE_COMMIT;
            }
        } else if (message.matches(MessageType.PREPARE, viewNumber)) {
            PrepareMessage prepareMessage = (PrepareMessage) message;
            if (!prepareMessage.getNode().getParent().equals(prepareMessage.getJustify().getNode())) return;
            if (!safeNode(prepareMessage.getNode(), prepareMessage.getJustify())) return;
            sendMessage(new PrepareVote(viewNumber, prepareMessage.getNode(), getNodeId()), getLeaderId());
            currentPhase = BHotStuffPhase.PRE_COMMIT;
        }
    }

    public void handleMessagePreCommit(String sender, AbstractMessage message){
        if (isLeader()) {
            if (message.matches(MessageType.PREPARE_VOTE, viewNumber, currentProposal))
                prepareVotes.add((PrepareVote) message);

            if (prepareVotes.size() >= getMinValidReplicas()) {
                var signature = new QuorumSignature();
                prepareQC = new QuorumCertificate(MessageType.PREPARE, viewNumber, currentProposal, signature);
                broadcastMessage(new PreCommitMessage(viewNumber, prepareQC));
                prepareVotes.clear();
                currentPhase = BHotStuffPhase.COMMIT;
            }
        } else if (message.matches(MessageType.PRE_COMMIT, viewNumber)) {
            PreCommitMessage preCommitMessage = (PreCommitMessage) message;
            if (preCommitMessage.getJustify().match(MessageType.PREPARE, viewNumber)) {
                prepareQC = preCommitMessage.getJustify();
                sendMessage(new PreCommitVote(viewNumber, prepareQC.getNode(), getNodeId()), getLeaderId());
                currentPhase = BHotStuffPhase.COMMIT;
            }
        }
    }

    public void handleMessageCommit(String sender, AbstractMessage message) throws Exception {
        if (isLeader()) {
            if (message.matches(MessageType.PRE_COMMIT_VOTE, viewNumber, prepareQC.getNode()))
                preCommitVotes.add((PreCommitVote) message);

            if (preCommitVotes.size() >= getMinValidReplicas()) {
                var signature = new QuorumSignature();
                preCommitQC = new QuorumCertificate(MessageType.PRE_COMMIT, viewNumber, prepareQC.getNode(), signature);
                broadcastMessage(new CommitMessage(viewNumber, preCommitQC));
                preCommitVotes.clear();
                currentPhase = BHotStuffPhase.DECIDE;
            }
        } else if (message.matches(MessageType.COMMIT, viewNumber, prepareQC.getNode())) {
            CommitMessage commitMessage = (CommitMessage) message;
            if (commitMessage.getJustify().match(MessageType.PRE_COMMIT, viewNumber)) {
                lockedQC = commitMessage.getJustify();
                sendMessage(new CommitVote(viewNumber, lockedQC.getNode(), getNodeId()), getLeaderId());
                currentPhase = BHotStuffPhase.DECIDE;
            }
        }
    }

    public void handleMessageDecide(String sender, AbstractMessage message) throws Exception {
        if (isLeader()) {
            if (message.matches(MessageType.COMMIT_VOTE, viewNumber, preCommitQC.getNode()))
                commitVotes.add((CommitVote) message);

            if (commitVotes.size() >= getMinValidReplicas()) {
                var signature = new QuorumSignature();
                commitQC = new QuorumCertificate(MessageType.COMMIT, viewNumber, preCommitQC.getNode(), signature);
                broadcastMessage(new DecideMessage(viewNumber, commitQC));
                commitVotes.clear();
                currentPhase = BHotStuffPhase.PREPARE;
            }
        } else if (message.matches(MessageType.DECIDE, viewNumber, lockedQC.getNode())) {
            DecideMessage decideMessage = (DecideMessage) message;
            if (decideMessage.getJustify().match(MessageType.COMMIT, viewNumber)) {
                commitOperation(new SerializableLogEntry(decideMessage.getJustify().getNode().getCmd()));
                // TODO: Respond to clients
                currentPhase = BHotStuffPhase.PREPARE;
            }
        }
    }

    public boolean isLeader() {
        return getLeaderId().equals(getNodeId());
    }

    public int getMinValidReplicas() {
        int n = getNodeIds().size();
        int f = (int) Math.floor((double)(n - 1) / 3);
        return n - f;
    }

    private Node createLeaf(Node parent, String cmd) {
        Node child = new Node();
        child.setParent(parent);
        child.setCmd(cmd);
        return child;
    }

    private Boolean safeNode(Node node, QuorumCertificate qc) {
        return node.getParent().equals(lockedQC.getNode()) || qc.getViewNumber() > lockedQC.getViewNumber();
    }
}