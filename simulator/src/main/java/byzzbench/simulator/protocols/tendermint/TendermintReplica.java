package byzzbench.simulator.protocols.tendermint;

import byzzbench.simulator.LeaderBasedProtocolReplica;
import byzzbench.simulator.Scenario;
import byzzbench.simulator.protocols.tendermint.message.ReplyMessage;
import byzzbench.simulator.protocols.tendermint.message.RequestMessage;
import byzzbench.simulator.protocols.tendermint.message.*;
import byzzbench.simulator.state.TotalOrderCommitLog;
import byzzbench.simulator.transport.DefaultClientRequestPayload;
import byzzbench.simulator.transport.MessagePayload;


import java.io.Serializable;
import java.time.Duration;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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

    private final long tolerance = 1;

    // Assigned powers of each replica in the network
    private final Map<String, Integer> votingPower = new HashMap<>();

    private boolean enoughPrecommitsCheck;
    private boolean preVoteFirstTime;
    private boolean prevoteOrMoreFirstTime;

    public static final Block NULL_BLOCK = new Block(Long.MIN_VALUE, Long.MIN_VALUE, Long.MIN_VALUE, "NULL VALUE", null);

    public final int TIMEOUT = 10;

    public Random rand = new Random(2137L);


    public TendermintReplica(String nodeId, SortedSet<String> nodeIds, Scenario scenario) {
        // Initialize replica with node ID, a list of other nodes, transport, and a commit log
        super(nodeId, scenario, new TotalOrderCommitLog());
        this.height = 0;
        this.round = 0;
        this.step = Step.NONE;
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
    }

    //====================================================================================================
    // PROPOSE Step
    //====================================================================================================

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
            return;
        } else {
            broadcastGossipProposal(proposalMessage);
        }

        // Find the proposal for the current height and round
        ProposalMessage proposal = messageLog.getProposals().values().stream().flatMap(List::stream) // Flatten lists of ProposalMessages
                .filter(p -> p.getHeight() == height).filter(p -> p.getRound() == round).filter(p -> p.getValidRound() == -1).filter(p -> p.getReplicaId().equals(proposer(this.height, this.round))).findFirst().orElse(null);

        if (proposal != null) {
            if (valid(proposal.getBlock()) && (lockedRound <= proposal.getValidRound() || lockedValue.equals(proposal.getBlock()))) {
                broadcastPrevote(height, round, proposal.getBlock());
            } else {
                broadcastPrevote(height, round, NULL_BLOCK);
            }
            step = Step.PREVOTE;
        }

        // Process proposals with sufficient prevotes
        proposal = messageLog.getProposals().values().stream().flatMap(List::stream).filter(p -> p.getHeight() == height && p.getRound() == round && p.getReplicaId().equals(proposer(this.height, this.round))).findFirst().orElse(null);

        if (proposal != null) {
            ProposalMessage finalProposal = proposal;
            if (messageLog.getPrevotes().values().stream().flatMap(List::stream).filter(prevote -> prevote.getHeight() == height && prevote.getRound() == finalProposal.getValidRound() && prevote.getBlock().equals(finalProposal.getBlock())).count() >= 2 * tolerance + 1) {
                if (this.step == Step.PREVOTE && (proposal.getValidRound() >= 0 && proposal.getValidRound() < this.round)) {
                    if (valid(proposal.getBlock()) && (lockedRound <= proposal.getValidRound() || lockedValue.equals(proposal.getBlock()))) {
                        broadcastPrevote(height, round, proposal.getBlock());
                    } else {
                        broadcastPrevote(height, round, NULL_BLOCK);
                    }
                }
            }
        }

        // Process proposals for precommits
        proposal = messageLog.getProposals().values().stream().flatMap(List::stream).filter(p -> p.getHeight() == height && p.getRound() == round && p.getReplicaId().equals(proposer(this.height, this.round))).findFirst().orElse(null);

        if (proposal != null) {
            ProposalMessage finalProposal1 = proposal;
            if (messageLog.getPrevotes().values().stream().flatMap(List::stream).filter(prevote -> prevote.getHeight() == height && prevote.getRound() == this.round && prevote.getBlock().equals(finalProposal1.getBlock())).count() >= 2 * tolerance + 1) {
                if (valid(proposal.getBlock()) && preVoteFirstTime) {
                    preVoteFirstTime = false;
                    if (this.step == Step.PREVOTE) {
                        lockedValue = proposal.getBlock();
                        lockedRound = this.round;
                        broadcastPrecommit(height, round, proposal.getBlock());
                        step = Step.PRECOMMIT;
                    }
                    validValue = proposal.getBlock();
                    validRound = this.round;
                }
            }
        }

        // Process proposals with sufficient precommits
        proposal = messageLog.getProposals().values().stream().flatMap(List::stream).filter(p -> p.getHeight() == height && p.getReplicaId().equals(proposer(this.height, this.round))).findFirst().orElse(null);

        if (proposal != null) {
            ProposalMessage finalProposal = proposal;
            if (messageLog.getPrecommits().values().stream().flatMap(List::stream).filter(precommit -> precommit.getHeight() == height && precommit.getRound() == finalProposal.getRound() && precommit.getBlock().equals(finalProposal.getBlock())).count() >= 2 * tolerance + 1) {
                if (getCommitLog().get((int) height) == null && valid(proposal.getBlock())) {
                    log.info("Committing block: " + proposal.getBlock());
                    commitOperation(proposal.getBlock());
                    messageLog.removeRequest(proposal.getBlock());
                    ReplyMessage replyMessage = new ReplyMessage(
                            this.height,
                            proposal.getBlock().getRequestMessage().getTimestamp(),
                            proposal.getBlock().getRequestMessage().getClientId(),
                            this.getId(),
                            proposal.getBlock().getRequestMessage().getOperation());
                    log.info("Sending reply: " + replyMessage);
                    sendReply(proposal.getBlock().getRequestMessage().getClientId(), replyMessage);
                    height++;
                    reset();
                    startRound(0);
                }
            }
        }
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
     * Broadcasts the block proposal to all validators in the network.
     * Intended Behavior:
     * - Send a proposal message containing the block details to all peers.
     */
    protected void broadcastProposal(long height, long round, Block proposal, long validRound) {
        log.info("Broadcasting proposal as leader: " + getId());
//        messageLog.sentProposal();
        ProposalMessage proposalMessage = new ProposalMessage(getId(), height, round, validRound, proposal);
        broadcastMessage(proposalMessage);
    }

    private void onTimeoutPropose(long height, long round) {
        if (this.height == height && this.round == round && this.step == Step.PROPOSE) {
            broadcastPrevote(this.height, this.round, NULL_BLOCK);
            this.step = Step.PREVOTE;
        }
    }

    //====================================================================================================
    // PREVOTE Step
    //====================================================================================================

    /**
     * Handles a prevote messages.
     *
     * @param prevoteMessage
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
        boolean[] uponRules = new boolean[4];

        Set<Block> blocks_height_2fPlus1 = getBlocks_height_2fPlus1();
        Set<ProposalMessage> proposals_height_round_validRound = getProposals_height_round_validRound(blocks_height_2fPlus1);
        boolean uponRule0Condition = this.step.equals(Step.PROPOSE) && (prevoteMessage.getRound() >= 0 && prevoteMessage.getRound() < this.round);
        uponRules[0] = (!blocks_height_2fPlus1.isEmpty() && !proposals_height_round_validRound.isEmpty() && uponRule0Condition);

        boolean uponRule1Condition = this.step.equals(Step.PREVOTE) && preVoteFirstTime;
        uponRules[1] = getAnyBlocks_height_round_2fPlus1() && uponRule1Condition;

        // Get blocks with enough prevotes in the current round
        Set<Block> blocks_height_round_2fPlus1 = getBlocks_height_round_2fPlus1(); // Collect into a Set
        Set<ProposalMessage> proposals_height_round = getProposals_height_round(blocks_height_round_2fPlus1);
        boolean uponRule2Condition = this.step.compareTo(Step.PREVOTE) >= 0 && prevoteOrMoreFirstTime;
        uponRules[2] = (!blocks_height_round_2fPlus1.isEmpty() && !proposals_height_round.isEmpty() && uponRule2Condition);

        // Handle NULL_BLOCK cases
        Set<Block> nullBlocks_height_round_2fPlus1 = getNullBlocks_height_round_2fPlus1(); // Collect into a Set
        boolean uponRule3Condition = this.step.equals(Step.PREVOTE);
        uponRules[3] = (!nullBlocks_height_round_2fPlus1.isEmpty() && uponRule3Condition);
        log.info("Upon rules: " + Arrays.toString(uponRules));

        prevoteRandomOrderExecute(uponRules, proposals_height_round_validRound, proposals_height_round, nullBlocks_height_round_2fPlus1);
    }

    private void executePrevoteRule0(Set<ProposalMessage> matchingProposals) {
        for (ProposalMessage matchingProposal : matchingProposals) {
            if (matchingProposal != null) {
                if (valid(matchingProposal.getBlock()) && (lockedRound <= matchingProposal.getValidRound() || lockedValue.equals(matchingProposal.getBlock()))) {
                    broadcastPrevote(height, round, matchingProposal.getBlock());
                } else {
                    broadcastPrevote(height, round, NULL_BLOCK);
                }
                step = Step.PREVOTE;
            }
        }
    }

    private void executePrevoteRule1() {
        preVoteFirstTime = false;
        Duration duration = Duration.ofSeconds(this.TIMEOUT);
        this.setTimeout("Timeout Prevote", () -> {
            this.onTimeoutPrevote(height, round);
        }, duration);
    }

    private void executePrevoteRule2(Set<ProposalMessage> matchingProposals) {
        for (ProposalMessage matchingProposal : matchingProposals) {
            if (matchingProposal != null) {
                if (valid(matchingProposal.getBlock())) {
                    prevoteOrMoreFirstTime = false;
                    if (this.step == Step.PREVOTE) {
                        lockedValue = matchingProposal.getBlock();
                        lockedRound = this.round;
                        broadcastPrecommit(height, round, matchingProposal.getBlock());
                        step = Step.PRECOMMIT;
                    }
                    validValue = matchingProposal.getBlock();
                    validRound = this.round;
                }
            }
        }
    }

    private void executePrevoteRule3(Set<Block> nullBlocksHeightRound2fPlus1) {
        if (step.equals(Step.PREVOTE)) {
            broadcastPrecommit(height, round, NULL_BLOCK);
            step = Step.PRECOMMIT;
        }
    }

    private void prevoteRandomOrderExecute(boolean[] uponRules,
                                           Set<ProposalMessage> proposals_height_round_validRound,
                                           Set<ProposalMessage> proposals_height_round,
                                           Set<Block> nullBlocksHeightRound2fPlus1) {
        List<Integer> trueIndices = new ArrayList<>();

        // Collect indices of true values
        for (int i = 0; i < uponRules.length; i++) {
            if (uponRules[i]) {
                trueIndices.add(i);
            }
        }

        // If there are no true values, return
        if (trueIndices.isEmpty()) {
            log.info("No Prevote rule to execute");
            return;
        }

        // Shuffle the indices to randomize the execution order
        Collections.shuffle(trueIndices, rand);

        log.info("Shuffled indices for execution: " + trueIndices.toString());

        // Execute each rule in the randomized order
        for (int index : trueIndices) {
            switch (index) {
                case 0:
                    log.info("Executing Prevote Rule 0");
                    executePrevoteRule0(proposals_height_round_validRound);
                    break;
                case 1:
                    log.info("Executing Prevote Rule 1");
                    executePrevoteRule1();
                    break;
                case 2:
                    log.info("Executing Prevote Rule 2");
                    executePrevoteRule2(proposals_height_round);
                    break;
                case 3:
                    log.info("Executing Prevote Rule 3");
                    executePrevoteRule3(nullBlocksHeightRound2fPlus1);
                    break;
                default:
                    log.warning("Unexpected index: " + index);
                    break;
            }
        }
    }


    private boolean getAnyBlocks_height_round_2fPlus1() {
        return messageLog.getPrevotesCount() >= (2 * tolerance + 1);
    }

    private Set<Block> getNullBlocks_height_round_2fPlus1() {
        return messageLog.getPrevotes().entrySet().stream()
                .filter(entry -> entry.getKey().getHeight() == height) // Match height
                .filter(entry -> entry.getKey().getRound() == round) // Match current round
                .filter(entry -> entry.getKey().equals(NULL_BLOCK)) // Check for NULL_BLOCK
                .filter(entry -> entry.getValue().size() >= 2 * tolerance + 1) // At least 2f + 1 prevotes
                .map(Map.Entry::getKey) // Extract the block
                .collect(Collectors.toSet());
    }

    private Set<ProposalMessage> getProposals_height_round(Set<Block> blocksHeightRound2fPlus1) {
        Set<ProposalMessage> matchingProposals = new HashSet<>();
        for (Block block : blocksHeightRound2fPlus1) {
            ProposalMessage matchingProposal = messageLog.getProposals().values().stream().flatMap(List::stream) // Flatten proposals
                    .filter(proposal -> proposal.getHeight() == height) // Match height
                    .filter(proposal -> proposal.getRound() == round) // Match round
                    .filter(proposal -> proposal.getBlock().equals(block)) // Match block
                    .filter(proposal -> proposal.getReplicaId().equals(proposer(this.height, this.round))) // Match proposer
                    .findFirst().orElse(null);
            if (matchingProposal != null) {
                matchingProposals.add(matchingProposal);
            }
        }
        return matchingProposals;
    }

    private Set<Block> getBlocks_height_round_2fPlus1() {
        return messageLog.getPrevotes().entrySet().stream()
                .filter(entry -> entry.getKey().getHeight() == height) // Match height
                .filter(entry -> entry.getKey().getRound() == round) // Match current round
                .filter(entry -> entry.getValue().size() >= 2 * tolerance + 1) // At least 2f + 1 prevotes
                .map(Map.Entry::getKey) // Extract the block
                .collect(Collectors.toSet());
    }

    private Set<ProposalMessage> getProposals_height_round_validRound(Set<Block> blocksWithEnoughPrevotes) {
        Set<ProposalMessage> matchingProposals = new HashSet<>();
        for (Block block : blocksWithEnoughPrevotes) {
            ProposalMessage matchingProposal = messageLog.getProposals().values().stream().flatMap(List::stream) // Flatten proposals
                    .filter(proposal -> proposal.getHeight() == height) // Match height
                    .filter(proposal -> proposal.getRound() == round) // Match round
                    .filter(proposal -> proposal.getBlock().equals(block)) // Match block
                    .filter(proposal -> proposal.getValidRound() == (block.getRound()))
                    .filter(proposal -> proposal.getReplicaId().equals(proposer(this.height, this.round))) // Match proposer
                    .findFirst().orElse(null);
            if (matchingProposal != null) {
                matchingProposals.add(matchingProposal);
            }
        }
        return matchingProposals;
    }

    private Set<Block> getBlocks_height_2fPlus1() {
        // Get blocks that have at least 2f + 1 prevotes at the current height
        Set<Block> blocksWithEnoughPrevotes = messageLog.getPrevotes().entrySet().stream()
                .filter(entry -> entry.getKey().getHeight() == height) // Match height
                .filter(entry -> entry.getValue().size() >= 2 * tolerance + 1) // At least 2f + 1 prevotes
                .map(Map.Entry::getKey) // Extract the block
                .collect(Collectors.toSet()); // Collect into a Set
        return blocksWithEnoughPrevotes;
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
        PrevoteMessage prevoteMessage = new PrevoteMessage(height, round, this.getId(), block);
        broadcastMessage(prevoteMessage);
    }

    private void onTimeoutPrevote(long height, long round) {
        log.info("on timeout prevote exec");
        log.info(this.height + " " + height + " " + this.round + " " + round + " " + this.step);
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
        // Validate the message
        if (validateMessage(precommitMessage)) {
            log.warning("Invalid precommit received: " + precommitMessage);
            return;
        }
        // Add the precommit message to the log
        boolean added = messageLog.addMessage(precommitMessage);
        if (!added) {
            return;
        } else {
            broadcastGossipPrecommit(precommitMessage);
        }

        // Get sets of precommit messages with at least 2f + 1 precommits at the current height and round
        Set<List<PrecommitMessage>> precommitsWithEnoughVotes = messageLog.getPrecommits().entrySet().stream().filter(entry -> entry.getKey().getHeight() == height) // Match height
                .filter(entry -> entry.getKey().getRound() == round) // Match round
                .filter(entry -> entry.getValue().size() >= 2 * tolerance + 1) // At least 2f + 1 precommits
                .map(Map.Entry::getValue) // Extract the list of precommit messages
                .collect(Collectors.toSet()); // Collect into a Set

        // Process each set of precommit messages
        for (List<PrecommitMessage> precommitList : precommitsWithEnoughVotes) {
            if (enoughPrecommitsCheck) {
                enoughPrecommitsCheck = false;
                Duration duration = Duration.ofSeconds(this.TIMEOUT);
                this.setTimeout("Timeout Precommit", () -> this.onTimeoutPrecommit(height, round), duration);
            }
        }

        precommitsWithEnoughVotes = messageLog.getPrecommits().entrySet().stream().filter(entry -> entry.getKey().getHeight() == height) // Match height
                .filter(entry -> entry.getValue().size() >= 2 * tolerance + 1) // At least 2f + 1 precommits
                .map(Map.Entry::getValue) // Extract the block
                .collect(Collectors.toSet()); // Collect into a Set

        for (List<PrecommitMessage> precommitList : precommitsWithEnoughVotes) {
            for (PrecommitMessage precommit : precommitList) {
                ProposalMessage matchingProposal = messageLog.getProposals().values().stream().flatMap(List::stream) // Flatten proposals
                        .filter(proposal -> proposal.getHeight() == height) // Match height
                        .filter(proposal -> proposal.getRound() == round) // Match round
                        .filter(proposal -> proposal.getBlock().equals(precommit.getBlock())) // Match block
                        .filter(proposal -> proposal.getReplicaId().equals(proposer(this.height, this.round))) // Match proposer
                        .findFirst().orElse(null);
                if (matchingProposal != null) {
                    if (getCommitLog().get((int) height) == null && valid(precommit.getBlock())) {
                        commitOperation(precommit.getBlock());
                        messageLog.removeRequest(precommit.getBlock());
                        log.info("Committing block: " + precommit.getBlock());
                        ReplyMessage replyMessage = new ReplyMessage(
                                this.height,
                                matchingProposal.getBlock().getRequestMessage().getTimestamp(),
                                matchingProposal.getBlock().getRequestMessage().getClientId(),
                                getId(),
                                matchingProposal.getBlock().getRequestMessage().getOperation());
                        log.info("Sending reply: " + replyMessage);
                        sendReply(matchingProposal.getBlock().getRequestMessage().getClientId(), replyMessage);
                        height++;
                        reset();
                        startRound(0);
                    }
                }
            }
        }

    }


    private void broadcastGossipPrecommit(PrecommitMessage precommitMessage) {
        broadcastMessage(new GossipMessage(precommitMessage.getReplicaId(), precommitMessage));
    }

    protected void broadcastPrecommit(long height, long round, Block block) {
        PrecommitMessage precommitMessage = new PrecommitMessage(height, round, this.getId(), block);
        broadcastMessage(precommitMessage);
    }

    private void onTimeoutPrecommit(long height, long round) {
        if (this.height == height && this.round == round) {
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
        if (!(message instanceof ProposalMessage || message instanceof PrevoteMessage || message instanceof PrecommitMessage || message instanceof GossipMessage || message instanceof DefaultClientRequestPayload || message instanceof GossipRequest)) {
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
        if (message instanceof DefaultClientRequestPayload) {
            receiveRequest(sender, new RequestMessage(((DefaultClientRequestPayload) message).getOperation(), System.currentTimeMillis(), message.getSignedBy()));
            return;
        } else if (message instanceof GossipRequest) {
            handleGossipRequest((GossipRequest) message);
            return;
        }
        if (validateMessage(message)) {
            return;
        }

        if (message instanceof GossipMessage) {
            handleGossipMessage((GossipMessage) message);
        } else {
            assert message instanceof GenericMessage;
            if (messageLog.fPlus1MessagesInRound((GenericMessage) message, round)) {
                GenericMessage m = (GenericMessage) message;
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

    private void handleGossipRequest(GossipRequest message) {
        messageLog.bufferRequest(message.getRequest());
    }

    private void handleGossipMessage(GossipMessage message) {
        if (validateMessage(message.getGossipMessage())) {
            log.warning("Invalid message received from " + message.getReplicaId());
            return;
        }
        if (messageLog.fPlus1MessagesInRound(message.getGossipMessage(), round)) {
            startRound(message.getGossipMessage().getRound());
        }
        switch (message.getGossipMessage()) {
            case ProposalMessage proposalMessage -> handleProposal(proposalMessage);
            case PrevoteMessage prevoteMessage -> handlePrevote(prevoteMessage);
            case PrecommitMessage precommitMessage -> handlePrecommit(precommitMessage);
            case null, default -> log.warning("Unhandled message type: " + message.getGossipMessage().getType());
        }
    }

    /**
     * Starts a new round of the consensus protocol.
     * <p>
     * 11: Function StartRound(round) :
     * 12: roundp ← round
     * 13: stepp ← propose
     * 14: if proposer(hp, roundp) = p then
     * 15: if validV aluep ̸= nil then
     * 16: proposal ← validV aluep
     * 17: else
     * 18: proposal ← getV alue()
     * 19: broadcast ⟨PROPOSAL, hp, roundp, proposal, validRoundp⟩
     * 20: else
     * 21: schedule OnTimeoutPropose(hp, roundp) to be executed after timeoutPropose(roundp)
     *
     * @param roundNumber The round number to start.
     */
    private void startRound(long roundNumber) {
        log.info(this.getId() + ": START ROUND CALLED: " + roundNumber);
        this.setView(height, roundNumber);
        this.round = roundNumber;
        step = Step.PROPOSE;
        Block proposal = new Block(height, round, messageLog.getMessageCount() + 1, null, null);
        if (this.getLeaderId().equals(getId())) {
            if (validValue != null) {
                proposal = validValue;
            } else {
                proposal = getValue();
            }
            log.info("Proposal: " + proposal);
            if (proposal != null)
                broadcastProposal(height, round, proposal, validRound);
        } else {
            Duration duration = Duration.ofSeconds(this.TIMEOUT);
            this.setTimeout("Timeout Propose", () -> {
                onTimeoutPropose(height, round);
            }, duration);
        }

    }


    private Block getValue() {
        log.info("Getting value");

        // Retrieve the first request message from the message log
        Optional<RequestMessage> requestPayload = messageLog.getRequests().stream().findFirst();
        Set<RequestMessage> rs = messageLog.getRequests();
        for (RequestMessage r : rs){
            log.info(r.toString());
        }
        // If the requestPayload is empty, return a Block with default "NULL VALUE"
        if (requestPayload.isEmpty()) {
            return null;
        }

        // Extract the operation and clientId from the found requestPayload
        RequestMessage payload = requestPayload.get();
        String operationString = payload.getOperation().toString();
        String clientId = operationString.contains("/")
                ? operationString.split("/")[0]
                : "UNKNOWN_CLIENT";

        // Return a Block with the extracted values
        return new Block(
                height,
                round,
                messageLog.getMessageCount() + 1,
                operationString,
                new RequestMessage(payload.getOperation(), System.currentTimeMillis(), clientId)
        );
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
        long maxVotingPower = 0;
        for (Map.Entry<String, Integer> entry : votingPower.entrySet()) {
            maxVotingPower = Math.max(maxVotingPower, entry.getValue());
        }
        for (int i = 0; i < maxVotingPower; i++) {
            for (Map.Entry<String, Integer> entry : votingPower.entrySet()) {
                String node = entry.getKey();
                int power = entry.getValue();
                if (power > i) {
                    proposerList.add(node);
                }
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
        log.info("Received client request: " + request);
        RequestMessage m = new RequestMessage(request, System.currentTimeMillis(), clientId);
        startRound(0);
    }

    private void receiveRequest(String sender, RequestMessage m) {
        messageLog.bufferRequest(m);
        broadcastMessage(new GossipRequest(this.getId(), m));
        if(proposer(this.height, this.round).equals(this.getId()) && this.step == Step.NONE) {
            startRound(this.round);
        }
    }

    /**
     * Initializes the replica state and starts the consensus process.
     */
    @Override
    public void initialize() {
        log.info("Initialized Tendermint replica: " + getId());
        setView(0, 0);
    }

    public void setView(long height, long round) {
        log.info("Setting view: " + height);
        log.info("Proposer: " + proposer(height, round));
        this.setView(height, proposer(height, round));
    }

    public void sendReply(String clientId, ReplyMessage reply) {
        this.sendReplyToClient(clientId, reply);
    }
}