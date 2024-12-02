package byzzbench.simulator.protocols.tendermint;

import byzzbench.simulator.LeaderBasedProtocolReplica;
import byzzbench.simulator.protocols.tendermint.message.RequestMessage;
import byzzbench.simulator.protocols.tendermint.message.*;
import byzzbench.simulator.state.TotalOrderCommitLog;
import byzzbench.simulator.transport.MessagePayload;
import byzzbench.simulator.transport.Transport;


import java.io.Serializable;
import java.util.*;

import lombok.Getter;
import lombok.extern.java.Log;

@Log
@Getter
public class TendermintReplica extends LeaderBasedProtocolReplica {

    // Blockchain height: the current index of the chain being decided
    private long height;

    // Round: the current round within the height, where multiple rounds might be needed to finalize a block
    private long round;

    // Step: the current step within the round (PROPOSE, PREVOTE, PRECOMMIT)
    private Step step;

    // Hash of the block this replica is "locked" on (used to ensure no conflicting decisions are made)
    private Block lockedValue;

    // Round number of the block this replica is locked on
    private long lockedRound;

    // Hash of the block this replica has validated
    private Block validValue;

    // Round number of the block this replica has validated
    private long validRound;

    private MessageLog messageLog;

    private long tolerance = 1;

    // Assigned powers of each replica in the network
    private final Map<String, Integer> votingPower = new HashMap<>();

    private boolean enoughPrecommitsCheck;
    private boolean preVoteFirstTime;
    private boolean prevoteOrMoreFirstTime;

    private boolean readyToCommit;

    public static final Block NULL_BLOCK = new Block(Long.MIN_VALUE, Long.MIN_VALUE, Long.MIN_VALUE, "NULL VALUE");


    public TendermintReplica(String nodeId, SortedSet<String> nodeIds, Transport transport) {
        // Initialize replica with node ID, a list of other nodes, transport, and a commit log
        super(nodeId, nodeIds, transport, new TotalOrderCommitLog());
        this.height = 0;
        this.round = 0;
        this.step = Step.PROPOSE;
        this.lockedValue = null;
        this.lockedRound = -1;
        this.validValue = null;
        this.validRound = -1;
        this.messageLog = new MessageLog(this);
        this.enoughPrecommitsCheck = true;
        this.preVoteFirstTime = true;
        this.prevoteOrMoreFirstTime = true;
        this.votingPower.put("A", 3);
        this.votingPower.put("B", 2);
        this.votingPower.put("C", 1);
        this.votingPower.put("D", 1);
        this.readyToCommit = false;
    }

    /**
     * PROPOSE Step
     */

    /**
     * Handles the proposal from the designated leader for the current round.
     * Intended Behavior:
     * - Validate the proposal's height and round.
     * - Accept the proposal if valid, otherwise reject it.
     */
    protected void handleProposal(ProposalMessage proposalMessage) {
        // Process the proposal
        boolean added = messageLog.addMessage(proposalMessage);
        if (!added) {
            log.info("Proposal already received: " + proposalMessage);
            return;
        } else {
            log.info("Proposal added to log: " + proposalMessage);
            broadcastGossipProposal(proposalMessage);
        }

        log.info("The block is: " + proposalMessage.getBlock());
        log.info("Boolean 28: " + boolean28(proposalMessage));
        log.info("Boolean 36: " + boolean36(proposalMessage));
        log.info("Boolean 49: " + boolean49(proposalMessage));
        log.info("Is initial proposal: " + isInitialProposal(proposalMessage));

        if (boolean28(proposalMessage)) {
            handle28(proposalMessage);
        } else if (boolean36(proposalMessage)) {
            handle36(proposalMessage);
        } else if (isInitialProposal(proposalMessage)) {
            initialProposal(proposalMessage);
        } else if (boolean49(proposalMessage)) {
            handle49(proposalMessage);
        } else {
            log.warning("Invalid proposal received: " + proposalMessage);
        }
//        // 22: upon ⟨PROPOSAL, hp, roundp, v, −1⟩ from proposer(hp, roundp) while stepp = propose do
//        if (validRoundIsNegative(proposalMessage)) {
//            log.info("The block is: " + proposalMessage.getBlock());
//            log.info("Boolean 49: " + boolean49(proposalMessage));
//            log.info("Boolean 36: " + boolean36(proposalMessage));
//            if (boolean49(proposalMessage)) {
//                handle49(proposalMessage);
//            }
//            else if (boolean36(proposalMessage)) {
//                handle36(proposalMessage);
//            }
//            else {
//                initialProposal(proposalMessage);
//            }
//        } else {
//            log.info("The block is: " + proposalMessage.getBlock());
//            // 28: 2f + 1 ⟨PREVOTE, hp, vr, id(v)⟩ while stepp = propose ∧ (vr ≥ 0 ∧ vr < roundp) do
//            log.info("Boolean 28: " + boolean28(proposalMessage));
//            log.info("Boolean 36: " + boolean36(proposalMessage));
//            log.info("Boolean 49: " + boolean49(proposalMessage));
//            if (boolean28(proposalMessage)) {
//                handle28(proposalMessage);
//                // 36: 2f + 1 ⟨PREVOTE, hp, roundp, id(v)⟩ while valid(v) ∧ stepp ≥ prevote for the first time do
//            } else if (boolean36(proposalMessage)) {
//                handle36(proposalMessage);
//            } else if (boolean49(proposalMessage)) {
//                handle49(proposalMessage);
//            }
//            else{
//                log.warning("Invalid proposal received: " + proposalMessage);
//            }
//        }

    }

    private void handle28(ProposalMessage proposalMessage) {
        log.info("2f + 1 ⟨PREVOTE, hp, vr, id(v)⟩ while stepp = propose ∧ (vr ≥ 0 ∧ vr < roundp)");
        if (valid(proposalMessage.getBlock())
                && (lockedRound <= proposalMessage.getValidRound() || lockedValue.equals(proposalMessage.getBlock()))) {
            broadcastPrevote(height, round, proposalMessage.getBlock());
        } else {
            broadcastPrevote(height, round, NULL_BLOCK);
        }
        step = Step.PREVOTE;
    }

    private boolean boolean28(ProposalMessage proposalMessage) {
        return proposalMessage.isSignedBy(proposer(height, proposalMessage.getRound()))
                && messageLog.hasEnoughPreVotes(new Block(height, proposalMessage.getValidRound(), proposalMessage.getBlock().getId(), proposalMessage.getBlock().getValue()))
                && this.step == Step.PROPOSE
                && (proposalMessage.getValidRound() >= 0 && proposalMessage.getValidRound() < this.round);
    }

    private void handle36(ProposalMessage proposalMessage) {
        this.prevoteOrMoreFirstTime = false;
        log.info("2f + 1 ⟨PREVOTE, hp, roundp, id(v)⟩ while valid(v) ∧ stepp ≥ prevote for the first time");
        if (this.step == Step.PREVOTE) {
            this.lockedValue = proposalMessage.getBlock();
            this.lockedRound = this.round;
            broadcastPrecommit(height, round, proposalMessage.getBlock());
            this.step = Step.PRECOMMIT;
        }
        this.validValue = proposalMessage.getBlock();
        this.validRound = this.round;
    }

    private boolean boolean36(ProposalMessage proposalMessage) {
        return proposalMessage.isSignedBy(proposer(height, round))
                && messageLog.hasEnoughPreVotes(new Block(height, round, proposalMessage.getBlock().getId(), proposalMessage.getBlock().getValue()))
                && valid(proposalMessage.getBlock())
                && (this.step.compareTo(Step.PREVOTE) >= 0 && this.prevoteOrMoreFirstTime);
    }

    private void handle49(ProposalMessage proposalMessage) {
        log.info("AND 2f + 1 ⟨PRECOMMIT, hp, r, id(v)⟩ while decisionp[hp] = nil");
        if (valid(proposalMessage.getBlock())) {
            commitOperation(proposalMessage.getBlock());
            height++;
            reset();
            startRound(0);
        }
    }

    private boolean boolean49(ProposalMessage proposalMessage) {
        return proposalMessage.isSignedBy(proposer(height, round))
                && messageLog.hasEnoughPreCommits(new Block(height, proposalMessage.getRound(), proposalMessage.getBlock().getId(), proposalMessage.getBlock().getValue()))
                && getCommitLog().get((int) height) == null;
    }

    private void initialProposal(ProposalMessage proposalMessage) {
        if (valid(proposalMessage.getBlock())
            && this.lockedRound == -1 || this.lockedValue.equals(proposalMessage.getBlock())) {
            broadcastPrevote(height, round, proposalMessage.getBlock());
        } else {
            broadcastPrevote(height, round, NULL_BLOCK);
        }
        this.step = Step.PREVOTE;
    }

    private void broadcastGossipProposal(ProposalMessage proposalMessage) {
        broadcastMessage(new GossipMessage(proposalMessage.getReplicaId(), proposalMessage));
    }

    private void reset() {
        this.lockedValue = null;
        this.lockedRound = -1;
        this.validValue = null;
        this.validRound = -1;
        this.enoughPrecommitsCheck = true;
        this.preVoteFirstTime = true;
        this.prevoteOrMoreFirstTime = true;
        this.messageLog = new MessageLog(this);
    }

    /**
     * This variant of the Byzantine consensus problem has an application-specific valid() predicate to indicate
     * whether a value is valid. In the context of blockchain systems, for example, a value is not valid if it does not
     * contain an appropriate hash of the last value (block) added to the blockchain.
     */
    private boolean valid(Block block) {
        return true;
    }

    /**
     * ⟨PROPOSAL, hp, roundp, v, −1⟩ from proposer(hp, roundp) while stepp = propose do
     *
     * @param proposalMessage - the proposal message
     * @return true if the proposal is the initial proposal
     */
    private boolean isInitialProposal(ProposalMessage proposalMessage) {
        return proposalMessage.isSignedBy(proposer(height, round))
                && proposalMessage.getValidRound() == -1
                && step == Step.PROPOSE;
    }

    /**
     * Broadcasts the block proposal to all validators in the network.
     * Intended Behavior:
     * - Send a proposal message containing the block details to all peers.
     */
    protected void broadcastProposal(long height, long round, Block proposal, long validRound) {
        log.info("Broadcasting proposal as leader: " + getNodeId());
//        messageLog.sentProposal();
        ProposalMessage proposalMessage = new ProposalMessage(getNodeId(), height, round, validRound, proposal);
        broadcastMessage(proposalMessage);
    }

    private void onTimeoutPropose(long height, long round) {
        if (this.height == height && this.round == round && this.step == Step.PROPOSE) {
            broadcastPrevote(this.height, this.round, NULL_BLOCK);
            this.step = Step.PREVOTE;
        }
    }

    /**
     * PREVOTE Step
     */

    /**
     * TODO: implement the step below
     * Condition:
     * 34: upon 2f + 1 ⟨PREVOTE, hp, roundp, ∗⟩ while stepp = prevote for the first time do
     * 35: schedule OnT imeoutP revote(hp, roundp) to be executed after timeoutP revote(roundp)
     * Another condition:
     * 44: upon 2f + 1 ⟨PREVOTE, hp, roundp, nil⟩ while stepp = prevote do
     * 45: broadcast ⟨PRECOMMIT, hp, roundp, nil⟩
     * 46: stepp ← precommit
     */
    protected void handlePrevote(PrevoteMessage prevoteMessage) {
        if (validateMessage(prevoteMessage)) {
            log.warning("Invalid prevote received: " + prevoteMessage);
            return;
        }

        boolean added = messageLog.addMessage(prevoteMessage);
        if (!added) {
            return;
        } else {
            broadcastGossipPrevote(prevoteMessage);
        }
        log.info("Prevote received: " + prevoteMessage);

        if (messageLog.hasEnoughPreVotes(prevoteMessage.getBlock())) {
            log.info("Quorum reached at replica: " + this.getNodeId() + " for block id: " + prevoteMessage.getBlock().getId());
            log.info("Quorum reached for block hash: " + prevoteMessage.getBlock());
            if (this.step == Step.PREVOTE && this.preVoteFirstTime) {
                log.info("on timeout prevote");
                onTimeoutPrevote(height, round);
                this.preVoteFirstTime = false;
            } else if (this.step == Step.PREVOTE) {
                log.info("Broadcasting precommit for null block block: ");
                broadcastPrecommit(height, round, NULL_BLOCK);
                this.step = Step.PRECOMMIT;
            }
        } else {
            log.info(this.getNodeId() + " Did not have enough prevotes for: " + prevoteMessage.getBlock().getId());
        }
    }

    private void broadcastGossipPrevote(PrevoteMessage prevoteMessage) {
        broadcastMessage(new GossipMessage(prevoteMessage.getReplicaId(), prevoteMessage));
    }

    /**
     * Broadcasts a prevote message for the current round.
     * Intended Behavior:
     * - Vote for the locked block, proposed block, or nil, depending on the state.
     */
    private void broadcastPrevote(long height, long round, Block block) {
//        messageLog.sentPrevote();
        PrevoteMessage prevoteMessage = new PrevoteMessage(height, round, this.getNodeId(), block);
        broadcastMessage(prevoteMessage);
    }

    private void onTimeoutPrevote(long height, long round) {
        if (this.height == height && this.round == round && this.step == Step.PREVOTE) {
            broadcastPrecommit(height, round, NULL_BLOCK);
            this.step = Step.PRECOMMIT;
        }
    }


    /**
     * PRECOMMIT Step
     */

    /**
     * Handles a precommit message during the precommit step.
     * Intended Behavior:
     * - Validate and track precommits for the block.
     * - If 2/3 of validators precommit, commit the block.
     */
    protected void handlePrecommit(PrecommitMessage precommitMessage) {
        if (validateMessage(precommitMessage)) {
            return;
        }

        boolean added = messageLog.addMessage(precommitMessage);
        if (!added) {
            return;
        } else {
            broadcastGossipPrecommit(precommitMessage);
        }

        if (messageLog.hasEnoughPreCommits(new Block(height, round, precommitMessage.getBlock().getId(), precommitMessage.getBlock().getValue()))
                && this.enoughPrecommitsCheck) {
            this.enoughPrecommitsCheck = false;
            log.info("on timeout precommit");
            onTimeoutPrecommit(height, round);
        }
    }

    private void broadcastGossipPrecommit(PrecommitMessage precommitMessage) {
        broadcastMessage(new GossipMessage(precommitMessage.getReplicaId(), precommitMessage));
    }

    protected void broadcastPrecommit(long height, long round, Block block) {
//        messageLog.sentPrecommit();
        PrecommitMessage precommitMessage = new PrecommitMessage(height, round, this.getNodeId(), block);
        broadcastMessage(precommitMessage);
    }

    private void onTimeoutPrecommit(long height, long round) {
        if (this.height == height
                && this.round == round
                && this.step == Step.PRECOMMIT) {
            startRound(this.round + 1);
        }
    }


    /**
     * MISCELLANEOUS
     */

    /**
     * Validates an incoming message for correctness.
     * <p>
     * This includes checks for message type, blockchain height, and round number.
     *
     * @param message The message to validate.
     * @return True if valid, false otherwise.
     */
    protected boolean validateMessage(MessagePayload message) {
        // Check if the message type matches a known type
        if (!(message instanceof ProposalMessage || message instanceof PrevoteMessage || message instanceof PrecommitMessage || message instanceof GossipMessage)) {
            log.warning("Unknown message type: " + message.getType());
            return true;
        }

        // Check that the message height matches the current height
        if (message instanceof GenericMessage) {
            if (((GenericMessage) message).getHeight() != height) {
                log.warning("Message height mismatch: " + ((GenericMessage) message).getHeight());
                return true;
            }
        }

        return false;
    }

    /**
     * Handles receipt of a generic Tendermint message.
     * <p>
     * Routes the message to the appropriate handler based on its type.
     *
     * @param sender  The ID of the sender.
     * @param message The received message.
     * @throws Exception If an error occurs while handling the message.
     */
    @Override
    public void handleMessage(String sender, MessagePayload message) throws Exception {
        if (validateMessage(message)) {
            log.warning("Invalid message received from " + sender);
            return;
        }

        if (message instanceof GossipMessage) {
            handleGossipMessage((GossipMessage) message);
        } else {
            if (messageLog.fPlus1MessagesInRound((GenericMessage) message, round)) {
                GenericMessage m = (GenericMessage) message;
                log.info("f + 1 messages received in round: " + m.getRound());
                startRound(m.getRound());
            } else {
                if (message instanceof ProposalMessage) {
                    handleProposal((ProposalMessage) message);
                } else if (message instanceof PrevoteMessage) {
                    handlePrevote((PrevoteMessage) message);
                } else if (message instanceof PrecommitMessage) {
                    log.info("Gonna deal with a precommit message");
                    handlePrecommit((PrecommitMessage) message);
                } else {
                    log.warning("Unhandled message type: " + message.getType());
                }
            }
        }
    }

    private void handleGossipMessage(GossipMessage message) {
        if (validateMessage(message.getGossipMessage())) {
            log.warning("Invalid message received from " + message.getReplicaId());
            return;
        }
        if (messageLog.fPlus1MessagesInRound(message.getGossipMessage(), round)) {
            startRound(message.getGossipMessage().getRound());
        }
        if (message.getGossipMessage() instanceof ProposalMessage) {
            handleProposal((ProposalMessage) message.getGossipMessage());
        } else if (message.getGossipMessage() instanceof PrevoteMessage) {
            handlePrevote((PrevoteMessage) message.getGossipMessage());
        } else if (message.getGossipMessage() instanceof PrecommitMessage) {
            handlePrecommit((PrecommitMessage) message.getGossipMessage());
        } else {
            log.warning("Unhandled message type: " + message.getGossipMessage().getType());
        }
    }

    /**
     * Starts a new round of the consensus protocol.
     *
     * @param roundNumber The round number to start.
     */
    private void startRound(long roundNumber) {
        log.info("START ROUND CALLED: " + roundNumber);
        this.round = roundNumber;
        step = Step.PROPOSE;
        Block proposal = new Block(height, round, messageLog.getMessageCount() + 1, null);
        if (proposer(height, roundNumber).equals(getNodeId())) {
            if (validValue != null) {
                proposal = validValue;
            } else {
                // TODO: implement getValue()
                proposal = getValue();
            }
            broadcastProposal(height, round, proposal, validRound);
        } else {
            // WARNING: this only will work when the timeout is implemented
            // onTimeoutPropose(height, round);
        }

    }


    private Block getValue() {
        return new Block(height, round, messageLog.getMessageCount() + 1, "value");
    }

    /**
     * Determines the proposer in a deterministic, weighted round-robin manner.
     *
     * @param height The current height of the blockchain.
     * @param round  The current round of proposer selection.
     * @return The node ID that is selected as proposer.
     */
    private String proposer(long height, long round) {
        // Generate the proposer list based on voting power
        List<String> proposerList = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : votingPower.entrySet()) {
            String node = entry.getKey();
            int power = entry.getValue();
            for (int i = 0; i < power; i++) {
                proposerList.add(node);
            }
        }

        // Calculate the index based on the height and round
        // Using both height and round ensures determinism across rounds and heights
        int index = (int) ((height + round) % proposerList.size());

        return proposerList.get(index);
    }


    /**
     * CLIENT REQUESTS
     */

    /**
     * Handles a client request.
     *
     * @param clientId The ID of the client.
     * @param request  The request payload.
     * @throws Exception If an error occurs while handling the request.
     */
    @Override
    public void handleClientRequest(String clientId, Serializable request) throws Exception {
        // FIXME: should not get timestamp from system time
        RequestMessage m = new RequestMessage(request, System.currentTimeMillis(), clientId);
        broadcastMessage(m);
    }

    private void receiveRequest(RequestMessage m) {
        if (proposer(height, round).equals(getNodeId())) {
            startRound(0);
        }
    }

    /**
     * Initializes the replica state and starts the consensus process.
     */
    @Override
    public void initialize() {
        log.info("Initializing Tendermint replica: " + getNodeId());
        startRound(0);
    }
}