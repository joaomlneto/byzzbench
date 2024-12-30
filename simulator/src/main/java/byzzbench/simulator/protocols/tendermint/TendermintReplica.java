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
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
     * PROPOSE STEP
     */

    protected void handleProposal(ProposalMessage proposalMessage) {
        // Process the proposal
        boolean added = messageLog.addMessage(proposalMessage);
        if (!added) {
            return;
        } else {
            broadcastGossipProposal(proposalMessage);
        }

        boolean[] uponRules = new boolean[4];

        // Check if the proposal fulfills rule number 1
        if (proposalMessage.getHeight() == height
                && proposalMessage.getRound() == round
                && proposalMessage.getValidRound() == -1
                && proposalMessage.getReplicaId().equals(getLeaderId())
                && step == Step.PROPOSE) {
            log.info("Rule 0 is true");
            uponRules[0] = true;
        }

        // Check if the proposal fulfills rule number 2
        if (proposalMessage.getHeight() == height
                && proposalMessage.getRound() == round) {

            boolean enoughPrevotes = messageLog.getPrevotes().values().stream().flatMap(List::stream)
                    .filter(prevote -> prevote.getHeight() == height)
                    .filter(prevote -> prevote.getRound() == proposalMessage.getValidRound())
                    .filter(prevote -> prevote.getBlock().equals(proposalMessage.getBlock()))
                    .count() >= 2 * tolerance + 1;

            if (enoughPrevotes) {
                log.info("Rule 1 is true");
                uponRules[1] = true;
            }
        }

        // Check if the proposal fulfills rule number 3
        if (proposalMessage.getHeight() == height
                && proposalMessage.getRound() == round
                && proposalMessage.getReplicaId().equals(getLeaderId())) {
            boolean enoughPrevotes = messageLog.getPrevotes().values().stream().flatMap(List::stream)
                    .filter(prevote -> prevote.getHeight() == height)
                    .filter(prevote -> prevote.getRound() == this.round)
                    .filter(prevote -> prevote.getBlock().equals(proposalMessage.getBlock()))
                    .count() >= 2 * tolerance + 1;

            if (enoughPrevotes) {
                log.info("Rule 2 is true");
                uponRules[2] = true;
            }
        }

        // Check if the proposal fulfills rule number 4

        if (proposalMessage.getHeight() == height
                && proposalMessage.getReplicaId().equals(proposer(height, proposalMessage.getRound()))) {
            boolean enoughPrecommits = messageLog.getPrevotes().values().stream().flatMap(List::stream)
                    .filter(prevote -> prevote.getHeight() == height)
                    .filter(prevote -> prevote.getRound() == proposalMessage.getRound())
                    .filter(prevote -> prevote.getBlock().equals(proposalMessage.getBlock()))
                    .count() >= 2 * tolerance + 1;
            boolean decisionMade = getCommitLog().get((int) height) != null;
            if (enoughPrecommits && !decisionMade) {
                log.info("Rule 3 is true");
                uponRules[3] = true;
            }
        }

        // Execute the rules in a random order
        proposalRandomOrderExecute(uponRules, proposalMessage);

    }

    private void proposalRandomOrderExecute(boolean[] uponRules, ProposalMessage proposalMessage) {
        List<Integer> trueIndices = new ArrayList<>();

        // Collect indices of true values
        for (int i = 0; i < uponRules.length; i++) {
            if (uponRules[i]) {
                trueIndices.add(i);
            }
        }

        // If there are no true values, return
        if (trueIndices.isEmpty()) {
            log.info("No Proposal rule to execute");
            return;
        }

        // Shuffle the indices to randomize the execution order
        Collections.shuffle(trueIndices, rand);

        log.info("Shuffled indices for execution: " + trueIndices.toString());

        // Execute each rule in the randomized order
        for (int index : trueIndices) {
            switch (index) {
                case 0:
                    log.info("Executing Proposal Rule 0");
                    executeProposalRule(proposalMessage);
                    break;
                case 1:
                    log.info("Executing Proposal Rule 1");
                    executeProposalPrevoteRule1(proposalMessage);
                    break;
                case 2:
                    log.info("Executing Proposal Rule 2");
                    executeProposalPrevoteRule2(proposalMessage);
                    break;
                case 3:
                    log.info("Executing Proposal Rule 3");
                    executeProposalPrecommitRule(proposalMessage.getBlock());
                    break;
                default:
                    log.warning("Unexpected index: " + index);
                    break;
            }
        }
    }

    private void executeProposalPrecommitRule(Block block) {
        if (valid(block)) {
            commitOperation(block);
            messageLog.removeRequest(block);
            ReplyMessage replyMessage = new ReplyMessage(
                    this.height,
                    block.getRequestMessage().getTimestamp(),
                    block.getRequestMessage().getClientId(),
                    this.getId(),
                    block.getRequestMessage().getOperation());
            sendReply(block.getRequestMessage().getClientId(), replyMessage);
            height++;
            reset();
            startRound(0);
        }
    }

    private void executeProposalPrevoteRule2(ProposalMessage proposalMessage) {
        // Execution logic
        if (valid(proposalMessage.getBlock())
                && prevoteOrMoreFirstTime) {
            prevoteOrMoreFirstTime = false;
            if (this.step == Step.PREVOTE) {
                lockedValue = proposalMessage.getBlock();
                lockedRound = this.round;
                broadcastPrecommit(height, round, proposalMessage.getBlock());
                step = Step.PRECOMMIT;
            }
            validValue = proposalMessage.getBlock();
            validRound = this.round;
        }
    }

    private void executeProposalPrevoteRule1(ProposalMessage proposalMessage) {
        if (this.step == Step.PROPOSE
                && (proposalMessage.getValidRound() >= 0
                && proposalMessage.getValidRound() < this.round)) {
            if (valid(proposalMessage.getBlock())
                    && (lockedRound <= proposalMessage.getValidRound()
                    || lockedValue.equals(proposalMessage.getBlock()))) {
                broadcastPrevote(height, round, proposalMessage.getBlock());
            } else {
                broadcastPrevote(height, round, NULL_BLOCK);
            }
        }
    }

    private void executeProposalRule(ProposalMessage proposalMessage) {
        if (valid(proposalMessage.getBlock()) && (lockedRound <= proposalMessage.getValidRound() || lockedValue.equals(proposalMessage.getBlock()))) {
            broadcastPrevote(height, round, proposalMessage.getBlock());
        } else {
            broadcastPrevote(height, round, NULL_BLOCK);
        }
        step = Step.PREVOTE;
    }

    private void broadcastGossipProposal(ProposalMessage proposalMessage) {
        broadcastMessage(new GossipMessage(proposalMessage.getReplicaId(), proposalMessage));
    }

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

    /**
     * PREVOTE STEP
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
        Block blockFromPrevote = prevoteMessage.getBlock();

        // Fetch proposalFromBlock safely
        ProposalMessage proposalFromBlock;
        List<ProposalMessage> proposalsForBlock = messageLog.getProposals().get(blockFromPrevote);
        if (proposalsForBlock != null) {
            proposalFromBlock = proposalsForBlock.stream()
                    .filter(proposal -> proposal.getHeight() == height)
                    .filter(proposal -> proposal.getRound() == round)
                    .filter(proposal -> proposal.getReplicaId().equals(proposer(this.height, this.round)))
                    .findFirst().orElse(null);
        } else {
            proposalFromBlock = null;
        }

        // Prevote Rule 0
        if (proposalFromBlock != null) {
            boolean enoughPrevotes = messageLog.getPrevotes().values().stream().flatMap(list -> list != null ? list.stream() : Stream.empty())
                    .filter(prevote -> prevote.getHeight() == height)
                    .filter(prevote -> prevote.getRound() == proposalFromBlock.getValidRound())
                    .filter(prevote -> prevote.getBlock().equals(proposalFromBlock.getBlock()))
                    .count() >= 2 * tolerance + 1;
            if (this.step.equals(Step.PROPOSE) && (prevoteMessage.getRound() >= 0 && prevoteMessage.getRound() < this.round) && enoughPrevotes) {
                uponRules[0] = true;
            }
        }

        // Prevote Rule 1
        boolean uponRule1Condition = this.step.equals(Step.PREVOTE) && preVoteFirstTime;
        uponRules[1] = getAnyBlocks_height_round_2fPlus1() && uponRule1Condition;

        // Prevote Rule 2
        if (proposalFromBlock != null) {
            boolean enoughPrevotes = messageLog.getPrevotes().values().stream().flatMap(list -> list != null ? list.stream() : Stream.empty())
                    .filter(prevote -> prevote.getHeight() == height)
                    .filter(prevote -> prevote.getRound() == round)
                    .filter(prevote -> prevote.getBlock().equals(proposalFromBlock.getBlock()))
                    .count() >= 2 * tolerance + 1;
            if (valid(proposalFromBlock.getBlock()) && prevoteOrMoreFirstTime && enoughPrevotes) {
                uponRules[2] = true;
            }
        }

        // Prevote Rule 3
        if (step.equals(Step.PREVOTE)) {
            // Get the list of prevotes for NULL_BLOCK safely
            List<PrevoteMessage> nullBlockPrevotes = messageLog.getPrevotes().get(NULL_BLOCK);

            // Check if the list is not null
            if (nullBlockPrevotes != null) {
                boolean enoughPrevotesForNull = nullBlockPrevotes.stream()
                        .filter(prevote -> prevote.getHeight() == height)
                        .filter(prevote -> prevote.getRound() == round)
                        .count() >= 2 * tolerance + 1;

                if (enoughPrevotesForNull) {
                    uponRules[3] = true;
                }
            } else {
                log.warning("No prevotes found for NULL_BLOCK in the current messageLog.");
            }
        }

        prevoteRandomOrderExecute(uponRules, proposalFromBlock);
    }

    private void executePrevoteRule1() {
        preVoteFirstTime = false;
        Duration duration = Duration.ofSeconds(this.TIMEOUT);
        this.setTimeout("Timeout Prevote", () -> {
            this.onTimeoutPrevote(height, round);
        }, duration);
    }

    private void executePrevoteRule2() {
        if (step.equals(Step.PREVOTE)) {
            broadcastPrecommit(height, round, NULL_BLOCK);
            step = Step.PRECOMMIT;
        }
    }

    private void prevoteRandomOrderExecute(boolean[] uponRules, ProposalMessage proposalMessage) {
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
                    executeProposalPrevoteRule1(proposalMessage);
                    break;
                case 1:
                    log.info("Executing Prevote Rule 1");
                    executePrevoteRule1();
                    break;
                case 2:
                    log.info("Executing Prevote Rule 2");
                    executeProposalPrevoteRule2(proposalMessage);
                    break;
                case 3:
                    log.info("Executing Prevote Rule 3");
                    executePrevoteRule2();
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

    private void broadcastGossipPrevote(PrevoteMessage prevoteMessage) {
        broadcastMessage(new GossipMessage(prevoteMessage.getReplicaId(), prevoteMessage));
    }

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
     * PRECOMMIT STEP
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

        boolean[] uponRules = new boolean[2];

        // Precommit Rule 0
        if(fulfillPrecommitRule0()) {
            uponRules[0] = true;
        }

        // Precommit Rule 1
        if (fulfillPrecommitRule1(precommitMessage)) {
            uponRules[1] = true;
        }

        precommitRandomOrderExecute(uponRules, precommitMessage.getBlock());
    }

    private boolean fulfillPrecommitRule1(PrecommitMessage precommitMessage) {
        boolean enoughPrecommits = messageLog.getPrecommits().get(precommitMessage.getBlock()).stream()
                .filter(precommit -> precommit.getHeight() == precommit.getHeight())
                .filter(precommit -> precommit.getRound() == precommit.getRound())
                .count() >= 2 * tolerance + 1;

        boolean proposalExists = messageLog.getProposals().get(precommitMessage.getBlock()).stream()
                .filter(proposal -> proposal.getHeight() == precommitMessage.getHeight())
                .filter(proposal -> proposal.getRound() == precommitMessage.getRound())
                .filter(proposal -> proposal.getReplicaId().equals(proposer(precommitMessage.getHeight(), precommitMessage.getRound())))
                .count() >= 1;

        boolean decisionIsNull = getCommitLog().get((int) precommitMessage.getHeight()) == null;

        return enoughPrecommits && proposalExists && decisionIsNull;
    }

    private boolean fulfillPrecommitRule0() {
        return messageLog.getPrecommits().values().stream().flatMap(List::stream)
                .filter(precommitMessage -> precommitMessage.getHeight() == height)
                .filter(precommitMessage -> precommitMessage.getRound() == round)
                .count() >= 2 * tolerance + 1;
    }

    private void precommitRandomOrderExecute(boolean[] uponRules, Block block) {
        List<Integer> trueIndices = new ArrayList<>();

        // Collect indices of true values
        for (int i = 0; i < uponRules.length; i++) {
            if (uponRules[i]) {
                trueIndices.add(i);
            }
        }

        // If there are no true values, return
        if (trueIndices.isEmpty()) {
            log.info("No Precommit rule to execute");
            return;
        }

        // Shuffle the indices to randomize the execution order
        Collections.shuffle(trueIndices, rand);

        log.info("Shuffled indices for execution: " + trueIndices.toString());

        // Execute each rule in the randomized order
        for (int index : trueIndices) {
            switch (index) {
                case 0:
                    log.info("Executing Precommit Rule 0");
                    executePrecommitRule0();
                    break;
                case 1:
                    log.info("Executing Precommit Rule 1");
                    executeProposalPrecommitRule(block);
                    break;
                default:
                    log.warning("Unexpected index: " + index);
                    break;
            }
        }
    }

    private void executePrecommitRule0() {
        Duration duration = Duration.ofSeconds(this.TIMEOUT);
        this.setTimeout("Timeout Precommit", () -> {
            onTimeoutPropose(height, round);
        }, duration);
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

    private void reset() {
        this.lockedValue = null;
        this.lockedRound = -1;
        this.validValue = null;
        this.validRound = -1;
        this.enoughPrecommitsCheck = true;
        this.preVoteFirstTime = true;
        this.prevoteOrMoreFirstTime = true;
    }

    private boolean valid(Block block) {
        return true;
    }

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
     * HANDLING MESSAGES
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
        if (proposer(this.height, this.round).equals(this.getId())) {
            startRound(this.round);
        }
    }

    /**
     * MISCELLANEOUS AND UTILITY METHODS
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
        for (RequestMessage r : rs) {
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