package byzzbench.simulator.protocols.tendermint;

import byzzbench.simulator.LeaderBasedProtocolReplica;
import byzzbench.simulator.protocols.tendermint.message.RequestMessage;
import byzzbench.simulator.protocols.tendermint.message.*;
import byzzbench.simulator.state.TotalOrderCommitLog;
import byzzbench.simulator.transport.MessagePayload;
import byzzbench.simulator.transport.Transport;


import java.io.Serializable;
import java.util.*;
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

    private long tolerance = 1;

    // Assigned powers of each replica in the network
    private final Map<String, Integer> votingPower = new HashMap<>();

    private boolean enoughPrecommitsCheck;
    private boolean preVoteFirstTime;
    private boolean prevoteOrMoreFirstTime;

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

        // Find the proposal for the current height and round
        ProposalMessage proposal = messageLog.getProposals().values().stream()
                .flatMap(List::stream) // Flatten lists of ProposalMessages
                .filter(p -> p.getHeight() == height)
                .filter(p -> p.getRound() == round)
                .filter(p -> p.getValidRound() == -1)
                .filter(p -> p.getReplicaId().equals(proposer(this.height, this.round)))
                .findFirst()
                .orElse(null);

        if (proposal != null) {
            if (valid(proposal.getBlock())
                    && (lockedRound <= proposal.getValidRound() || lockedValue.equals(proposal.getBlock()))) {
                broadcastPrevote(height, round, proposal.getBlock());
            } else {
                broadcastPrevote(height, round, NULL_BLOCK);
            }
            step = Step.PREVOTE;
        }

        // Process proposals with sufficient prevotes
        proposal = messageLog.getProposals().values().stream()
                .flatMap(List::stream)
                .filter(p -> p.getHeight() == height && p.getRound() == round
                        && p.getReplicaId().equals(proposer(this.height, this.round)))
                .findFirst()
                .orElse(null);

        if (proposal != null) {
            ProposalMessage finalProposal = proposal;
            if (messageLog.getPrevotes().values().stream()
                    .flatMap(List::stream)
                    .filter(prevote -> prevote.getHeight() == height
                            && prevote.getRound() == finalProposal.getValidRound()
                            && prevote.getBlock().equals(finalProposal.getBlock()))
                    .count() >= 2 * tolerance + 1) {
                if (this.step == Step.PREVOTE
                        && (proposal.getValidRound() >= 0 && proposal.getValidRound() < this.round)) {
                    if (valid(proposal.getBlock())
                            && (lockedRound <= proposal.getValidRound()
                            || lockedValue.equals(proposal.getBlock()))) {
                        broadcastPrevote(height, round, proposal.getBlock());
                    } else {
                        broadcastPrevote(height, round, NULL_BLOCK);
                    }
                }
            }
        }

        // Process proposals for precommits
        proposal = messageLog.getProposals().values().stream()
                .flatMap(List::stream)
                .filter(p -> p.getHeight() == height
                        && p.getRound() == round
                        && p.getReplicaId().equals(proposer(this.height, this.round)))
                .findFirst()
                .orElse(null);

        if (proposal != null) {
            ProposalMessage finalProposal1 = proposal;
            if (messageLog.getPrevotes().values().stream()
                    .flatMap(List::stream)
                    .filter(prevote -> prevote.getHeight() == height
                            && prevote.getRound() == this.round
                            && prevote.getBlock().equals(finalProposal1.getBlock()))
                    .count() >= 2 * tolerance + 1) {
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
        proposal = messageLog.getProposals().values().stream()
                .flatMap(List::stream)
                .filter(p -> p.getHeight() == height
                        && p.getReplicaId().equals(proposer(this.height, this.round)))
                .findFirst()
                .orElse(null);

        if (proposal != null) {
            ProposalMessage finalProposal = proposal;
            if (messageLog.getPrecommits().values().stream()
                    .flatMap(List::stream)
                    .filter(precommit -> precommit.getHeight() == height
                            && precommit.getRound() == finalProposal.getRound()
                            && precommit.getBlock().equals(finalProposal.getBlock()))
                    .count() >= 2 * tolerance + 1) {
                if (getCommitLog().get((int) height) == null && valid(proposal.getBlock())) {
                    commitOperation(proposal.getBlock());
                    height++;
                    reset();
                    startRound(0, null);
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

        // Get blocks that have at least 2f + 1 prevotes at the current height
        Set<Block> blocksWithEnoughPrevotes = messageLog.getPrevotes().entrySet().stream()
                .filter(entry -> entry.getKey().getHeight() == height) // Match height
                .filter(entry -> entry.getValue().size() >= 2 * tolerance + 1) // At least 2f + 1 prevotes
                .map(Map.Entry::getKey) // Extract the block
                .collect(Collectors.toSet()); // Collect into a Set

        // Process proposals matching prevotes for valid blocks
        for (Block block : blocksWithEnoughPrevotes) {
            ProposalMessage matchingProposal = messageLog.getProposals().values().stream()
                    .flatMap(List::stream) // Flatten proposals
                    .filter(proposal -> proposal.getHeight() == height) // Match height
                    .filter(proposal -> proposal.getRound() == round) // Match round
                    .filter(proposal -> proposal.getBlock().equals(block)) // Match block
                    .filter(proposal -> proposal.getReplicaId().equals(proposer(this.height, this.round))) // Match proposer
                    .findFirst()
                    .orElse(null);

            if (matchingProposal != null) {
                if (this.step == Step.PREVOTE
                        && (matchingProposal.getValidRound() >= 0 && matchingProposal.getValidRound() < this.round)) {
                    if (valid(matchingProposal.getBlock())
                            && (lockedRound <= matchingProposal.getValidRound()
                            || lockedValue.equals(matchingProposal.getBlock()))) {
                        broadcastPrevote(height, round, matchingProposal.getBlock());
                    } else {
                        broadcastPrevote(height, round, NULL_BLOCK);
                    }
                }
            }
        }

        // Get blocks with enough prevotes in the current round
        blocksWithEnoughPrevotes = messageLog.getPrevotes().entrySet().stream()
                .filter(entry -> entry.getKey().getHeight() == height) // Match height
                .filter(entry -> entry.getKey().getRound() == round) // Match current round
                .filter(entry -> entry.getValue().size() >= 2 * tolerance + 1) // At least 2f + 1 prevotes
                .map(Map.Entry::getKey) // Extract the block
                .collect(Collectors.toSet()); // Collect into a Set

        for (Block block : blocksWithEnoughPrevotes) {
            ProposalMessage matchingProposal = messageLog.getProposals().values().stream()
                    .flatMap(List::stream) // Flatten proposals
                    .filter(proposal -> proposal.getHeight() == height) // Match height
                    .filter(proposal -> proposal.getRound() == round) // Match round
                    .filter(proposal -> proposal.getBlock().equals(block)) // Match block
                    .filter(proposal -> proposal.getReplicaId().equals(proposer(this.height, this.round))) // Match proposer
                    .findFirst()
                    .orElse(null);

            if (matchingProposal != null) {
                if (valid(matchingProposal.getBlock()) && prevoteOrMoreFirstTime) {
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

        // Handle NULL_BLOCK cases
        blocksWithEnoughPrevotes = messageLog.getPrevotes().entrySet().stream()
                .filter(entry -> entry.getKey().getHeight() == height) // Match height
                .filter(entry -> entry.getKey().getRound() == round) // Match current round
                .filter(entry -> entry.getKey().equals(NULL_BLOCK)) // Check for NULL_BLOCK
                .filter(entry -> entry.getValue().size() >= 2 * tolerance + 1) // At least 2f + 1 prevotes
                .map(Map.Entry::getKey) // Extract the block
                .collect(Collectors.toSet()); // Collect into a Set

        for (Block block : blocksWithEnoughPrevotes) {
            if (this.step == Step.PREVOTE) {
                // broadcastPrecommit(height, round, NULL_BLOCK);
                // this.step = Step.PRECOMMIT;
            }
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
        // Validate the message
        if (validateMessage(precommitMessage)) {
            log.info("Invalid precommit received: " + precommitMessage);
            return;
        }
        // Add the precommit message to the log
        boolean added = messageLog.addMessage(precommitMessage);
        if (!added) {
            log.info("Precommit already received: " + precommitMessage);
            return;
        } else {
            log.info("Precommit added to log: " + precommitMessage);
            broadcastGossipPrecommit(precommitMessage);
        }

        log.info("Precommit received: " + precommitMessage);

        // Get sets of precommit messages with at least 2f + 1 precommits at the current height and round
        Set<List<PrecommitMessage>> precommitsWithEnoughVotes = messageLog.getPrecommits().entrySet().stream()
                .filter(entry -> entry.getKey().getHeight() == height) // Match height
                .filter(entry -> entry.getKey().getRound() == round) // Match round
                .filter(entry -> entry.getValue().size() >= 2 * tolerance + 1) // At least 2f + 1 precommits
                .map(Map.Entry::getValue) // Extract the list of precommit messages
                .collect(Collectors.toSet()); // Collect into a Set

        // Process each set of precommit messages
        for (List<PrecommitMessage> precommitList : precommitsWithEnoughVotes) {
            if (enoughPrecommitsCheck) {
                enoughPrecommitsCheck = false;
                log.info("Scheduled on timeout precommit for precommits: " + precommitList);
            }
        }

        precommitsWithEnoughVotes = messageLog.getPrecommits().entrySet().stream()
                .filter(entry -> entry.getKey().getHeight() == height) // Match height
                .filter(entry -> entry.getValue().size() >= 2 * tolerance + 1) // At least 2f + 1 precommits
                .map(Map.Entry::getValue) // Extract the block
                .collect(Collectors.toSet()); // Collect into a Set

        for (List<PrecommitMessage> precommitList : precommitsWithEnoughVotes) {
            for (PrecommitMessage precommit : precommitList) {
                ProposalMessage matchingProposal = messageLog.getProposals().values().stream()
                        .flatMap(List::stream) // Flatten proposals
                        .filter(proposal -> proposal.getHeight() == height) // Match height
                        .filter(proposal -> proposal.getRound() == round) // Match round
                        .filter(proposal -> proposal.getBlock().equals(precommit.getBlock())) // Match block
                        .filter(proposal -> proposal.getReplicaId().equals(proposer(this.height, this.round))) // Match proposer
                        .findFirst()
                        .orElse(null);
                if (matchingProposal != null) {
                    if (getCommitLog().get((int) height) == null && valid(precommit.getBlock())) {
                        commitOperation(precommit.getBlock());
//                        height++;
//                        reset();
//                        startRound(0, null);
                    }
                }
            }
        }

    }


    private void broadcastGossipPrecommit(PrecommitMessage precommitMessage) {
        broadcastMessage(new GossipMessage(precommitMessage.getReplicaId(), precommitMessage));
    }

    protected void broadcastPrecommit(long height, long round, Block block) {
        PrecommitMessage precommitMessage = new PrecommitMessage(height, round, this.getNodeId(), block);
        broadcastMessage(precommitMessage);
    }

    private void onTimeoutPrecommit(long height, long round) {
        if (this.height == height
                && this.round == round
                && this.step == Step.PRECOMMIT) {
            startRound(this.round + 1, null);
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
        if (message instanceof RequestMessage) {
            log.info("Received request message: " + message);
            receiveRequest((RequestMessage) message);
            return;
        }
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
                startRound(m.getRound(), null);
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
            startRound(message.getGossipMessage().getRound(), null);
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
     * 21: schedule OnTimeoutPropose(hp, roundp) to be executed after timeoutP ropose(roundp)
     *
     * @param roundNumber The round number to start.
     */
    private void startRound(long roundNumber, RequestMessage m) {
        log.info(this.getNodeId() + ": START ROUND CALLED: " + roundNumber);
        this.round = roundNumber;
        step = Step.PROPOSE;
        Block proposal = new Block(height, round, messageLog.getMessageCount() + 1, null);
        if (proposer(height, roundNumber).equals(getNodeId())) {
            if (validValue != null) {
                proposal = validValue;
            } else if (m != null) {
                proposal = new Block(height, round, messageLog.getMessageCount() + 1, m.getOperation().toString());
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
        startRound(0, m);
    }

    private void receiveRequest(RequestMessage m) {
        log.info("Received request: " + m);
        if (proposer(height, round).equals(getNodeId())) {
            startRound(0, null);
        }
    }

    /**
     * Initializes the replica state and starts the consensus process.
     */
    @Override
    public void initialize() {
        log.info("Initializing Tendermint replica: " + getNodeId());
    }
}