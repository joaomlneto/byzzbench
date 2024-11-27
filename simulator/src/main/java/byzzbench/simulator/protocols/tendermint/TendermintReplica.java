package byzzbench.simulator.protocols.tendermint;

import byzzbench.simulator.LeaderBasedProtocolReplica;
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

    private List<Long> decision;

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

    private boolean preCommitCheck;

    public TendermintReplica(String nodeId, SortedSet<String> nodeIds, Transport transport) {
        // Initialize replica with node ID, a list of other nodes, transport, and a commit log
        super(nodeId, nodeIds, transport, new TotalOrderCommitLog());
        this.height = 0;
        this.round = 0;
        this.step = Step.PROPOSE;
        this.decision = new ArrayList<>();
        this.lockedValue = null;
        this.lockedRound = -1;
        this.validValue = null;
        this.validRound = -1;
        this.messageLog = new MessageLog(this);
        this.preCommitCheck = true;
    }

    // ============================
    // Propose Step
    // ============================

    /**
     * Handles the proposal from the designated leader for the current round.
     * Intended Behavior:
     * - Validate the proposal's height and round.
     * - Accept the proposal if valid, otherwise reject it.
     */
    protected void handleProposal(ProposalMessage proposalMessage) {
        // Validate if the message is signed by the correct proposer
        if (!proposalMessage.isSignedBy(proposer(height, round))) {
            log.warning("Message not from proposer for this round: " + proposalMessage);
            return;
        }

        // Process the proposal
        log.info("Valid proposal received: " + proposalMessage);

        if (isInitialProposal(proposalMessage)) {
            if (valid(proposalMessage.getBlock())) {
                log.info("Initial proposal accepted: " + proposalMessage);
                broadcastPrevote(height, round, proposalMessage.getBlock());
            }
            else {
                log.info("Initial proposal rejected: " + proposalMessage);
                broadcastPrevote(height, round, null);
            }
            log.info("Initial proposal accepted: " + proposalMessage);
            this.step = Step.PREVOTE;
        } else if (isSubsequentProposal(proposalMessage)) {
            log.info("Subsequent proposal received: " + proposalMessage);

            // Verify if the proposal is valid and if it's in the right step to be accepted\
            // valid(v) ∧ stepp ≥ prevote for the first time do
            if (valid(proposalMessage.getBlock()) &&
                    (this.step.equals(Step.PREVOTE) || this.step.equals(Step.PRECOMMIT))) {

                // Lock the value if currently in the PREVOTE step
                if (this.step == Step.PREVOTE) {
                    this.lockedValue = proposalMessage.getBlock();
                    this.lockedRound = proposalMessage.getRound();
                    broadcastPrecommit(height, round, proposalMessage.getBlock());
                    this.step = Step.PRECOMMIT;
                }

                this.validValue = proposalMessage.getBlock();
                this.validRound = proposalMessage.getRound();
            } else if (this.decision.get((int) height) == null) {
                // Finalize decision if valid
                if (valid(proposalMessage.getBlock())) {
                    this.decision.set((int) height, proposalMessage.getBlock().getId());
                    this.height++;
                    reset();
                }
                // stepp = propose ∧ (vr ≥ 0 ∧ vr < roundp)
            } else if (getStep().equals(Step.PROPOSE)
                    && proposalMessage.getValidRound() >= 0
                    && proposalMessage.getValidRound() < getRound()) {

                // Validate subsequent proposals and possibly broadcast prevote
                // valid(v) ∧ (lockedRoundp ≤ vr ∨ lockedV aluep = v)
                if (valid(proposalMessage.getBlock()) &&
                        (lockedRound <= proposalMessage.getValidRound() ||
                                lockedValue.equals(proposalMessage.getBlock()))) {
                    broadcastPrevote(height, round, proposalMessage.getBlock());
                } else {
                    broadcastPrevote(height, round, null);
                }
                step = Step.PREVOTE;
            }
            broadcastPrevote(height, round, null);
        } else {
            log.warning("Invalid proposal received: " + proposalMessage);
            broadcastPrevote(height, round, null);
        }

        // Advance to the next step
        step = Step.PREVOTE;
    }


    private void reset() {
        this.lockedValue = null;
        this.lockedRound = -1;
        this.validValue = null;
        this.validRound = -1;
    }

    // This variant of the Byzantine consensus problem has an application-specific valid() predicate to indicate
    // whether a value is valid. In the context of blockchain systems, for example, a value is not valid if it does not
    // contain an appropriate hash of the last value (block) added to the blockchain.
    private boolean valid(Block block) {
        return true;
    }

    /**
     * ⟨PROPOSAL, hp, roundp, v, vr⟩ from proposer(hp, roundp) AND 2f + 1 ⟨PREVOTE, hp, vr, id(v)⟩
     * @param proposalMessage
     * @return true if the proposal is a subsequent proposal
     */
    private boolean isSubsequentProposal(ProposalMessage proposalMessage) {
        if (proposalMessage.getValidRound() != -1) {
            // TODO: 2f + 1 ⟨PREVOTE, hp, vr, id(v)⟩ messages received
            // for now just true
            return true;
        }
        return false;
    }

    /**
     * ⟨PROPOSAL, hp, roundp, v, −1⟩ from proposer(hp, roundp) while stepp = propose do
     * @param proposalMessage
     * @return true if the proposal is the initial proposal
     */
    private boolean isInitialProposal(ProposalMessage proposalMessage) {
        if (proposalMessage.getValidRound() == -1) {
            if (proposer(height, round).equals(proposalMessage.getSignedBy()) && step == Step.PROPOSE) {
                return true;
            }
            return false;
        }
        return false;
    }

    /**
     * Broadcasts the block proposal to all validators in the network.
     * Intended Behavior:
     * - Send a proposal message containing the block details to all peers.
     */
    protected void broadcastProposal(long height, long round, Block proposal, long validRound) {
        log.info("Broadcasting proposal as leader: " + getNodeId());
        ProposalMessage proposalMessage = new ProposalMessage(getNodeId(), height, round, validRound, proposal);
        broadcastMessage(proposalMessage);
    }

    // ============================
    // Prevote Step
    // ============================

    /**
     * TODO: implement the step below
     * 34: upon 2f + 1 ⟨PREVOTE, hp, roundp, ∗⟩ while stepp = prevote for the first time do
     * 35: schedule OnT imeoutP revote(hp, roundp) to be executed after timeoutP revote(roundp)
     *
     * 44: upon 2f + 1 ⟨PREVOTE, hp, roundp, nil⟩ while stepp = prevote do
     * 45: broadcast ⟨PRECOMMIT, hp, roundp, nil⟩
     * 46: stepp ← precommit
     */
    protected void handlePrevote(PrevoteMessage prevoteMessage) {
        if (validateMessage(prevoteMessage)) {
            log.warning("Invalid prevote received: " + prevoteMessage);
            return;
        }

        log.info("Prevote received: " + prevoteMessage);
        messageLog.addVoteMessage(prevoteMessage);

        if (messageLog.hasEnoughPreVotes(prevoteMessage)){
            log.info("Quorum reached for block hash: " + prevoteMessage.getBlock().toString());
            if (this.step == Step.PREVOTE) {
                broadcastPrecommit(height, round, null);
                this.step = Step.PRECOMMIT;
            }
        }
    }

    /**
     * Broadcasts a prevote message for the current round.
     * Intended Behavior:
     * - Vote for the locked block, proposed block, or nil, depending on the state.
     */
    private void broadcastPrevote(long height, long round, Block block) {
        PrevoteMessage prevoteMessage = new PrevoteMessage(height, round, this.getNodeId(), block);
        broadcastMessage(prevoteMessage);
    }

    // ============================
    // Precommit Step
    // ============================

    /**
     * Handles a precommit message during the precommit step.
     * Intended Behavior:
     * - Validate and track precommits for the block.
     * - If 2/3 of validators precommit, commit the block.
     */
    protected void handlePrecommit(PrecommitMessage precommitMessage) {
        if (validateMessage(precommitMessage)) {
            log.warning("Invalid precommit received: " + precommitMessage);
            return;
        }

        log.info("Precommit received: " + precommitMessage);
        messageLog.addVoteMessage(precommitMessage);

        if (messageLog.hasEnoughPreCommits(precommitMessage)) {
            log.info("Quorum reached for block hash: " + precommitMessage.getBlock());
            if (this.preCommitCheck) {
                onTimeoutPrecommit(height, round);
            }
            }
    }

    protected void broadcastPrecommit(long height, long round, Block block) {
        PrecommitMessage precommitMessage = new PrecommitMessage(this.getNodeId(), height, round, block);
        broadcastMessage(precommitMessage);
    }

    // ============================
    // Miscellaneous
    // ============================

    /**
     * Validates an incoming message for correctness.
     * <p>
     * This includes checks for message type, blockchain height, and round number.
     *
     * @param message The message to validate.
     * @return True if valid, false otherwise.
     */
    protected boolean validateMessage(GenericMessage message) {
        // Check if the message type matches a known type
        if (!(message instanceof ProposalMessage || message instanceof PrevoteMessage || message instanceof PrecommitMessage || message instanceof CommitMessage)) {
            log.warning("Unknown message type: " + message.getType());
            return true;
        }

        // Check that the message height matches the current height
        if (message instanceof GenericMessage) {
            if (message.getHeight() != height) {
                log.warning("Message height mismatch: " + message.getHeight());
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
        if (validateMessage((GenericMessage) message)) {
            log.warning("Invalid message received from " + sender);
            return;
        }
        if (moveOn((GenericMessage) message)) {
            startRound(((GenericMessage) message).getRound());
        }
        else {
            if (message instanceof ProposalMessage) {
                handleProposal((ProposalMessage) message);
            } else if (message instanceof PrevoteMessage) {
                handlePrevote((PrevoteMessage) message);
            } else if (message instanceof PrecommitMessage) {
                handlePrecommit((PrecommitMessage) message);
//            } else if (message instanceof CommitMessage) {
//                handleCommitMessage((CommitMessage) message);
            } else {
                log.warning("Unhandled message type: " + message.getType());
            }
        }
    }

    private boolean moveOn(GenericMessage message) {
        return message.getRound() > this.round;
    }

    private void onTimeoutPropose(long height, long round) {
        if (this.height == height && this.round == round && this.step == Step.PROPOSE) {
            broadcastPrevote(height, round, null);
            this.step = Step.PREVOTE;
        }
    }

    private void onTimeoutPrevote(long height, long round) {
        if (this.height == height && this.round == round && this.step == Step.PREVOTE) {
            broadcastPrecommit(height, round, null);
            this.step = Step.PRECOMMIT;
        }
    }

    private void onTimeoutPrecommit(long height, long round) {
        if (this.height == height && this.round == round && this.step == Step.PRECOMMIT) {
            startRound(round + 1);
        }
    }


    /**
     * Handles a client request.
     *
     * @param clientId The ID of the client.
     * @param request  The request payload.
     * @throws Exception If an error occurs while handling the request.
     */
    @Override
    public void handleClientRequest(String clientId, Serializable request) throws Exception {
        throw new UnsupportedOperationException("Client requests not supported in FastHotStuff");
    }

    /**
     * Initializes the replica state and starts the consensus process.
     */
    @Override
    public void initialize() {
        log.info("Initializing Tendermint replica: " + getNodeId());
        startRound(0);
    }

    /**
     * Starts a new round of the consensus protocol.
     *
     * @param roundNumber The round number to start.
     */
    private void startRound(long roundNumber) {
        setRound(roundNumber);
        step = Step.PROPOSE;
        Block proposal = new Block(height, messageLog.getVoteMessageCount()+1, null);
        if (proposer(height, roundNumber).equals(getNodeId())) {
            if (validValue != null) {
                proposal = validValue;
            } else {
                // TODO: implement getValue()
                proposal = getValue();
            }
            broadcastProposal(height, round, proposal, validRound);
        }

    }

    private Block getValue() {
        return new Block(height, messageLog.getVoteMessageCount()+1, "value");
    }

    private void setRound(long roundNumber) {
        String leaderId = proposer(height, roundNumber);
        // view is the same as round in Tendermint
        this.round = roundNumber;
    }

    /**
     * For now just returns A.
     * We assume that the proposer selection function is weighted round-robin, where processes are
     * rotated proportional to their voting power. A validator with more voting power is selected more frequently,
     * proportional to its power. More precisely, during a sequence of rounds of size n, every process is proposer
     * in a number of rounds equal to its voting power.
     */
    private String proposer(long height, long round) {
        return "A";
    }
}