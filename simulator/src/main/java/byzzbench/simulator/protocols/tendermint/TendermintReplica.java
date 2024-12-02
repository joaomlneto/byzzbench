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

    private boolean precommitsCheck;
    private boolean preVoteFirstTime;
    private boolean greaterThanPrevoteCheck;

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
        this.precommitsCheck = true;
        this.preVoteFirstTime = true;
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
        // Validate if the message is signed by the correct proposer
        if (!proposalMessage.isSignedBy(proposer(height, round))) {
            log.warning("Message not from proposer for this round: " + proposalMessage);
            return;
        }

        // Process the proposal
        log.info("Valid proposal received: " + proposalMessage);
        boolean added = messageLog.addMessage(proposalMessage);
        if (!added) {
            log.info("Proposal already received: " + proposalMessage);
            return;
        } else {
            log.info("Proposal added to log: " + proposalMessage);
            broadcastGossipProposal(proposalMessage);
        }
        // 22: upon ⟨PROPOSAL, hp, roundp, v, −1⟩ from proposer(hp, roundp) while stepp = propose do
        if (validRoundIsNegative(proposalMessage)) {
            initialProposal(proposalMessage);
        } else {
            // 28: 2f + 1 ⟨PREVOTE, hp, vr, id(v)⟩ while stepp = propose ∧ (vr ≥ 0 ∧ vr < roundp) do
            if (messageLog.hasEnoughPreVotes(new Block(height, proposalMessage.getValidRound(), proposalMessage.getBlock().getId(), proposalMessage.getBlock().getValue()))
                    && this.step == Step.PROPOSE
                    && proposalMessage.getValidRound() >= 0 && proposalMessage.getValidRound() < this.round) {
                log.info("2f + 1 ⟨PREVOTE, hp, vr, id(v)⟩ while stepp = propose ∧ (vr ≥ 0 ∧ vr < roundp)");
                if (valid(proposalMessage.getBlock())
                        && (lockedRound <= proposalMessage.getValidRound() || lockedValue.equals(proposalMessage.getBlock()))) {
                    broadcastPrevote(height, round, proposalMessage.getBlock());
                } else {
                    broadcastPrevote(height, round, NULL_BLOCK);
                }
                step = Step.PREVOTE;
                // 36: 2f + 1 ⟨PREVOTE, hp, roundp, id(v)⟩ while valid(v) ∧ stepp ≥ TODO: prevote for the first time
            } else if (messageLog.hasEnoughPreVotes(new Block(height, round, proposalMessage.getBlock().getId(), proposalMessage.getBlock().getValue()))
                    && valid(proposalMessage.getBlock())
                    && (this.step.compareTo(Step.PREVOTE) >= 0)) {
                log.info("2f + 1 ⟨PREVOTE, hp, roundp, id(v)⟩ while valid(v) ∧ stepp ≥ prevote for the first time");
                if (this.step == Step.PREVOTE ){
                    this.lockedValue = proposalMessage.getBlock();
                    this.lockedRound = this.round;
                    broadcastPrecommit(height, round, proposalMessage.getBlock());
                    this.step = Step.PRECOMMIT;
                }
                this.validValue = proposalMessage.getBlock();
                this.validRound = this.round;
            } else if (messageLog.hasEnoughPreCommits(new Block(height, proposalMessage.getRound(), proposalMessage.getBlock().getId(), proposalMessage.getBlock().getValue()))
                    && getCommitLog().get((int) height) == null) {
                log.info("AND 2f + 1 ⟨PRECOMMIT, hp, r, id(v)⟩ while decisionp[hp] = nil");
                if (valid(proposalMessage.getBlock())) {
                    commitOperation(proposalMessage.getBlock());
                    height++;
                    reset();
                    startRound(0);
                }
            }
            else{
                log.warning("Invalid proposal received: " + proposalMessage);
                broadcastPrevote(height, round, NULL_BLOCK);
            }
        }

    }

    private void initialProposal(ProposalMessage proposalMessage) {
        if (valid(proposalMessage.getBlock())) {
            log.info("[137] Initial proposal accepted : " + proposalMessage);
            broadcastPrevote(height, round, proposalMessage.getBlock());
        } else {
            log.info("[140] Initial proposal rejected: " + proposalMessage);
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
        this.precommitsCheck = true;
        this.preVoteFirstTime = true;
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
     * ⟨PROPOSAL, hp, roundp, v, vr⟩ from proposer(hp, roundp) AND 2f + 1 ⟨PREVOTE, hp, vr, id(v)⟩
     *
     * @param proposalMessage - the proposal message
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
     *
     * @param proposalMessage - the proposal message
     * @return true if the proposal is the initial proposal
     */
    private boolean validRoundIsNegative(ProposalMessage proposalMessage) {
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
            if (this.step == Step.PREVOTE && precommitsCheck) {
                log.info("on timeout prevote");
                onTimeoutPrevote(height, round);
                precommitsCheck = false;
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
            log.warning("Invalid precommit received: " + precommitMessage);
            return;
        }

        log.info("Valid precommit received: " + precommitMessage);
        boolean added = messageLog.addMessage(precommitMessage);
        if (!added) {
            return;
        } else {
            broadcastGossipPrecommit(precommitMessage);
        }

        if (messageLog.hasEnoughPreCommits(new Block(height, round, precommitMessage.getBlock().getId(), precommitMessage.getBlock().getValue()))
                && this.precommitsCheck) {
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
        if (this.height == height && this.round == round && this.step == Step.PRECOMMIT) {
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
            if (moveOn((GenericMessage) message)) {
                startRound(((GenericMessage) message).getRound());
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
        if (moveOn(message.getGossipMessage())) {
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

    private boolean moveOn(GenericMessage message) {
        return message.getRound() > this.round;
    }

    /**
     * Starts a new round of the consensus protocol.
     *
     * @param roundNumber The round number to start.
     */
    private void startRound(long roundNumber) {
        log.info("START ROUND CALLED: " + roundNumber);
        setRound(roundNumber);
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
        }
        else {
            // WARNING: this only will work when the timeout is implemented
            // onTimeoutPropose(height, round);
        }

    }


    private Block getValue() {
        return new Block(height, round, messageLog.getMessageCount() + 1, "value");
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