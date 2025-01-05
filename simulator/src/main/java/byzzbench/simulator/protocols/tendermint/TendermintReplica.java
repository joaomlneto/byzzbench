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

import lombok.Getter;
import lombok.extern.java.Log;
import org.apache.commons.lang3.tuple.Pair;

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

    private final MessageLog messageLog;

    private final long tolerance;

    // Assigned powers of each replica in the network
    private final SortedMap<String, Integer> votingPower;

    private boolean enoughPrecommitsCheck;
    private boolean preVoteFirstTime;
    private boolean prevoteOrMoreFirstTime;

    public static final Block NULL_BLOCK = new Block(
            Long.MIN_VALUE,
            Long.MIN_VALUE,
            Long.MIN_VALUE,
            "NULL VALUE",
            0,
            null);


    public final int TIMEOUT = 10;

    public Random rand = new Random(2137L);

    private SortedSet<Pair<Long, Long>> hasBroadcasted = new TreeSet<>();


    public TendermintReplica(String nodeId, SortedSet<String> nodeIds, Scenario scenario, long tolerance, List<Long> votingPowers) {
        // Initialize replica with node ID, a list of other nodes, transport, and a commit log
        super(nodeId, scenario, new TotalOrderCommitLog());
        this.height = 0;
        this.round = 0;
        this.step = Step.PROPOSE;
        this.lockedValue = null;
        this.lockedRound = -1;
        this.validValue = null;
        this.validRound = -1;
        this.enoughPrecommitsCheck = true;
        this.preVoteFirstTime = true;
        this.prevoteOrMoreFirstTime = true;
        this.votingPower = new TreeMap<>();

        Iterator<String> nodeIdsIterator = nodeIds.iterator();
        int index = 0;

        while (nodeIdsIterator.hasNext() && index < votingPowers.size()) {
            String currentNodeId = nodeIdsIterator.next();
            int votingPower = votingPowers.get(index).intValue();
            log.info("Node ID: " + currentNodeId + " Voting Power: " + votingPower);
            this.votingPower.put(currentNodeId, votingPower);
            index++;
        }

        if (index < votingPowers.size()) {
            log.warning("Not all voting powers were assigned; too many powers provided.");
        }
        if (nodeIdsIterator.hasNext()) {
            log.warning("Not all nodes received voting powers; too few powers provided.");
        }

        log.info("Voting Power: " + this.votingPower.toString());
        this.tolerance = tolerance;

        this.messageLog = new MessageLog(tolerance, this.votingPower);
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

        if (fulfillProposalRule0(proposalMessage.getBlock())) {
            uponRules[0] = true;
        }

        if (fulfillProposalRule1(proposalMessage)) {
            uponRules[1] = true;
        }

        if (fulfillProposalRule2(proposalMessage)) {
            uponRules[2] = true;
        }

        if (fulfillProposalRule3(proposalMessage)) {
            uponRules[3] = true;
        }

        // Execute the rules in a random order
        proposalRandomOrderExecute(uponRules, proposalMessage);

    }

    private boolean fulfillProposalRule0(Block block) {
        boolean proposalExists = messageLog.getProposals().getOrDefault(block, new ArrayList<>()).stream()
                .filter(proposal -> proposal.getHeight() == height)
                .filter(proposal -> proposal.getRound() == round)
                .filter(proposal -> proposal.getReplicaId().equals(proposer(height, round)))
                .filter(proposal -> proposal.getValidRound() == -1)
                .count() == 1;

        boolean b = this.step == Step.PROPOSE;
        return proposalExists && b;
    }

    private boolean fulfillProposalRule1(ProposalMessage proposalMessage) {
        // Check if the proposal exists
        boolean proposalExists = messageLog.getProposals().getOrDefault(proposalMessage.getBlock(), new ArrayList<>()).stream()
                .filter(proposal -> proposal.getHeight() == height)
                .filter(proposal -> proposal.getRound() == round)
                .filter(proposal -> proposal.getReplicaId().equals(proposer(height, round)))
                .count() == 1;

        // Calculate the total voting power of matching prevotes
        int totalVotingPower = messageLog.getPrevotes().getOrDefault(proposalMessage.getBlock(), new ArrayList<>()).stream()
                .filter(prevote -> prevote.getHeight() == height)
                .filter(prevote -> prevote.getRound() == proposalMessage.getValidRound())
                .mapToInt(prevote -> votingPower.getOrDefault(prevote.getReplicaId(), 0))
                .sum();

        boolean enoughPrevotes = totalVotingPower >= 2 * tolerance + 1;

        boolean b = this.step == Step.PROPOSE && proposalMessage.getRound() > 0 && proposalMessage.getRound() <= this.round;

        return proposalExists && enoughPrevotes && b;
    }


    private boolean fulfillProposalRule2(ProposalMessage proposalMessage) {
        // Check if the proposal exists
        boolean proposalExists = messageLog.getProposals().getOrDefault(proposalMessage.getBlock(), new ArrayList<>()).stream()
                .filter(proposal -> proposal.getHeight() == height)
                .filter(proposal -> proposal.getRound() == round)
                .filter(proposal -> proposal.getReplicaId().equals(proposer(height, round)))
                .count() == 1;

        // Calculate the total voting power of matching prevotes
        int totalVotingPower = messageLog.getPrevotes().getOrDefault(proposalMessage.getBlock(), new ArrayList<>()).stream()
                .filter(prevote -> prevote.getHeight() == height)
                .filter(prevote -> prevote.getRound() == proposalMessage.getRound())
                .mapToInt(prevote -> votingPower.getOrDefault(prevote.getReplicaId(), 0))
                .sum();

        boolean enoughPrevotes = totalVotingPower >= 2 * tolerance + 1;

        boolean b = valid(proposalMessage.getBlock())
                && (this.step == Step.PREVOTE || this.step == Step.PRECOMMIT)
                && this.prevoteOrMoreFirstTime;

        return proposalExists && enoughPrevotes && b;
    }


    private boolean fulfillProposalRule3(ProposalMessage proposalMessage) {
        // Check if the proposal exists
        boolean proposalExists = messageLog.getProposals().getOrDefault(proposalMessage.getBlock(), new ArrayList<>()).stream()
                .filter(proposal -> proposal.getHeight() == height)
                .filter(proposal -> proposal.getReplicaId().equals(proposer(height, proposalMessage.getRound())))
                .count() == 1;

        // Calculate the total voting power of matching precommits
        int totalVotingPower = messageLog.getPrecommits().getOrDefault(proposalMessage.getBlock(), new ArrayList<>()).stream()
                .filter(precommit -> precommit.getHeight() == height)
                .filter(precommit -> precommit.getRound() == proposalMessage.getRound())
                .mapToInt(precommit -> votingPower.getOrDefault(precommit.getReplicaId(), 0))
                .sum();

        boolean enoughPrecommits = totalVotingPower >= 2 * tolerance + 1;

        boolean decisionMade = getCommitLog().get((int) height) != null;

        return proposalExists && enoughPrecommits && !decisionMade;
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
                    executeProposalPrevoteRule1(proposalMessage.getBlock(), proposalMessage.getValidRound());
                    break;
                case 2:
                    log.info("Executing Proposal Rule 2");
                    executeProposalPrevoteRule2(proposalMessage.getBlock());
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

    private void executeProposalRule(ProposalMessage proposalMessage) {
        if (valid(proposalMessage.getBlock()) &&
                (lockedRound == -1 || lockedValue.equals(proposalMessage.getBlock()))) {
            broadcastPrevote(height, round, proposalMessage.getBlock());
        } else {
            broadcastPrevote(height, round, NULL_BLOCK);
        }
        this.step = Step.PREVOTE;
    }

    private void executeProposalPrevoteRule1(Block block, long validRound) {
        if (valid(block)
                && (lockedRound <= validRound || lockedValue.equals(block))) {
            broadcastPrevote(height, round, block);
        } else {
            broadcastPrevote(height, round, NULL_BLOCK);
        }
        this.step = Step.PREVOTE;
    }

    private void executeProposalPrevoteRule2(Block block) {
        // Execution logic
        prevoteOrMoreFirstTime = false;
        if (this.step == Step.PREVOTE) {
            lockedValue = block;
            lockedRound = this.round;
            broadcastPrecommit(height, round, block);
            this.step = Step.PRECOMMIT;
        }
        validValue = block;
        validRound = this.round;
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

    private void broadcastGossipProposal(ProposalMessage proposalMessage) {
        broadcastMessage(new GossipMessage(proposalMessage.getReplicaId(), proposalMessage));
    }

    protected void broadcastProposal(long height, long round, Block proposal, long validRound) {
        if(hasBroadcasted.contains(Pair.of(height, round))) {
            return;
        }
        log.info("Broadcasting proposal as leader: " + getId());
//        messageLog.sentProposal();
        ProposalMessage proposalMessage = new ProposalMessage(getId(), height, round, validRound, proposal);
        broadcastMessage(proposalMessage);
        hasBroadcasted.add(Pair.of(height, round));
    }

    private void onTimeoutPropose(long height, long round) {
        if (this.height == height
                && this.round == round
                && this.step == Step.PROPOSE) {
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

        if (fulfillPrevoteRule0(prevoteMessage)) {
            uponRules[0] = true;
        }

        if (fulfillPrevoteRule1(prevoteMessage)) {
            uponRules[1] = true;
        }

        if (fulfillPrevoteRule2(prevoteMessage)) {
            uponRules[2] = true;
        }

        if (fulfillPrevoteRule3()) {
            uponRules[3] = true;
        }

        prevoteRandomOrderExecute(uponRules, prevoteMessage);
    }

    private boolean fulfillPrevoteRule0(PrevoteMessage prevoteMessage) {
        // Filter matching prevotes
        List<PrevoteMessage> matchingPrevotes = messageLog.getPrevotes().getOrDefault(prevoteMessage.getBlock(), new ArrayList<>()).stream()
                .filter(prevote -> prevote.getHeight() == height)
                .collect(Collectors.toList());

        // Calculate the total voting power of the matching prevotes
        int totalVotingPower = matchingPrevotes.stream()
                .mapToInt(prevote -> votingPower.getOrDefault(prevote.getReplicaId(), 0)) // Get voting power for each replica
                .sum();
        log.info(prevoteMessage.toString() + " received " + totalVotingPower + " votes. It needs " + (2 * tolerance + 1) + " votes.");
        // Check if the total voting power meets the threshold
        boolean enoughPrevotes = totalVotingPower >= 2 * tolerance + 1;

        // Filter matching proposals
        boolean hasMatchingProposal = messageLog.getProposals().getOrDefault(prevoteMessage.getBlock(), new ArrayList<>()).stream()
                .filter(proposal -> proposal.getHeight() == this.height)
                .filter(proposal -> proposal.getRound() == this.round)
                .filter(proposal -> proposal.getReplicaId().equals(proposer(this.height, this.round)))
                .filter(proposal -> proposal.getValidRound() == prevoteMessage.getRound())
                .count() == 1;

        // Check additional conditions
        boolean b = this.step == Step.PROPOSE && prevoteMessage.getRound() > 0 && prevoteMessage.getRound() <= this.round;

        return enoughPrevotes && hasMatchingProposal && b;
    }

    private boolean fulfillPrevoteRule1(PrevoteMessage prevoteMessage) {
        // Filter matching prevotes and sum their voting power
        int totalVotingPower = messageLog.getPrevotes().getOrDefault(prevoteMessage.getBlock(), new ArrayList<>()).stream()
                .filter(prevote -> prevote.getHeight() == this.height)
                .filter(prevote -> prevote.getRound() == this.round)
                .mapToInt(prevote -> votingPower.getOrDefault(prevote.getReplicaId(), 0))
                .sum();

        log.info(prevoteMessage.toString() + " received " + totalVotingPower + " votes. It needs " + (2 * tolerance + 1) + " votes.");
        // Check if the total voting power meets the threshold
        boolean enoughPrevotes = totalVotingPower >= 2 * tolerance + 1;

        boolean rest = this.step == Step.PREVOTE && this.preVoteFirstTime;

        return enoughPrevotes && rest;
    }

    private boolean fulfillPrevoteRule2(PrevoteMessage prevoteMessage) {
        log.info("Checking Prevote Rule 2 as replica" + this.getId() );
        // Filter matching prevotes and sum their voting power
        int totalVotingPower = messageLog.getPrevotes().getOrDefault(prevoteMessage.getBlock(), new ArrayList<>()).stream()
                .filter(prevote -> prevote.getHeight() == height)
                .filter(prevote -> prevote.getRound() == round)
                .mapToInt(prevote -> votingPower.getOrDefault(prevote.getReplicaId(), 0))
                .sum();

        log.info(prevoteMessage.toString() + " received " + totalVotingPower + " votes. It needs " + (2 * tolerance + 1) + " votes.");

        // Check if the total voting power meets the threshold
        boolean enoughPrevotes = totalVotingPower >= 2 * tolerance + 1;
        log.info("enoughPrevotes: " + enoughPrevotes);

        boolean proposalExists = messageLog.getProposals().getOrDefault(prevoteMessage.getBlock(), new ArrayList<>()).stream()
                .filter(proposal -> proposal.getHeight() == height)
                .filter(proposal -> proposal.getRound() == round)
                .filter(proposal -> proposal.getReplicaId().equals(proposer(height, round)))
                .count() == 1;

        log.info("Checking if proposal exists: " + proposalExists);


        boolean b = valid(prevoteMessage.getBlock())
                && (this.step == Step.PREVOTE || this.step == Step.PRECOMMIT)
                && this.prevoteOrMoreFirstTime;
        log.info(valid(prevoteMessage.getBlock()) + " " + (this.step == Step.PREVOTE || this.step == Step.PRECOMMIT) + " " + this.prevoteOrMoreFirstTime);
        log.info("Checking the rest" + b);

        return enoughPrevotes && proposalExists && b;
    }

    private boolean fulfillPrevoteRule3() {
        // Filter matching prevotes and sum their voting power
        int totalVotingPower = messageLog.getPrevotes().getOrDefault(NULL_BLOCK, new ArrayList<>()).stream()
                .filter(prevote -> prevote.getHeight() == height)
                .filter(prevote -> prevote.getRound() == round)
                .mapToInt(prevote -> votingPower.getOrDefault(prevote.getReplicaId(), 0))
                .sum();
        log.info( " In total there were " + totalVotingPower + " votes for the null block");
        // Check if the total voting power meets the threshold
        boolean enoughPrevotes = totalVotingPower >= 2 * tolerance + 1;
        return enoughPrevotes;
    }


    private void prevoteRandomOrderExecute(boolean[] uponRules, PrevoteMessage prevoteMessage) {
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
                    executeProposalPrevoteRule1(prevoteMessage.getBlock(), prevoteMessage.getRound());
                    break;
                case 1:
                    log.info("Executing Prevote Rule 1");
                    executePrevoteRule1();
                    break;
                case 2:
                    log.info("Executing Prevote Rule 2");
                    executeProposalPrevoteRule2(prevoteMessage.getBlock());
                    break;
                case 3:
                    log.info("Executing Prevote Rule 3");
                    executePrevoteRule3();
                    break;
                default:
                    log.warning("Unexpected index: " + index);
                    break;
            }
        }
    }

    private void executePrevoteRule1() {
        this.preVoteFirstTime = false;
        Duration duration = Duration.ofSeconds(this.TIMEOUT);
        this.setTimeout("Timeout Prevote", () -> {
            log.info("Timeout Prevote called");
            onTimeoutPrevote(height, round);
        }, duration);
    }

    private void executePrevoteRule3() {
        broadcastPrecommit(height, round, NULL_BLOCK);
        this.step = Step.PRECOMMIT;
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
        if (this.height == height
                && this.round == round
                && this.step == Step.PREVOTE) {
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
        if (fulfillPrecommitRule()) {
            uponRules[0] = true;
        }

        // Precommit Rule 1
        if (fulfillProposalPrecommitRule(precommitMessage)) {
            uponRules[1] = true;
        }

        precommitRandomOrderExecute(uponRules, precommitMessage.getBlock());
    }

    private boolean fulfillProposalPrecommitRule(PrecommitMessage precommitMessage) {
        // Calculate the total voting power of matching precommits
        int totalVotingPower = messageLog.getPrecommits().getOrDefault(precommitMessage.getBlock(), new ArrayList<>()).stream()
                .filter(precommit -> precommit.getHeight() == this.height)
                .mapToInt(precommit -> votingPower.getOrDefault(precommit.getReplicaId(), 0))
                .sum();

        log.info(precommitMessage.toString() + " received " + totalVotingPower + " votes. It needs " + (2 * tolerance + 1) + " votes.");
        boolean enoughPrecommits = totalVotingPower >= 2 * tolerance + 1;

        boolean proposalExists = messageLog.getProposals().getOrDefault(precommitMessage.getBlock(), new ArrayList<>()).stream()
                .filter(proposal -> proposal.getHeight() == this.getHeight())
                .filter(proposal -> proposal.getRound() == precommitMessage.getRound())
                .filter(proposal -> proposal.getReplicaId().equals(proposer(precommitMessage.getHeight(), precommitMessage.getRound())))
                .count() == 1;

        boolean decisionIsNull = getCommitLog().get((int) precommitMessage.getHeight()) == null;

        return enoughPrecommits && proposalExists && decisionIsNull;
    }


    private boolean fulfillPrecommitRule() {
        // Calculate the total voting power of matching precommits
        int totalVotingPower = messageLog.getPrecommits().values().stream()
                .flatMap(List::stream) // Flatten the lists into a single stream
                .filter(precommitMessage -> precommitMessage.getHeight() == height)
                .filter(precommitMessage -> precommitMessage.getRound() == round)
                .mapToInt(precommitMessage -> votingPower.getOrDefault(precommitMessage.getReplicaId(), 0))
                .sum();
        log.info("Received " + totalVotingPower + " votes in total");
        boolean enoughPrecommits = totalVotingPower >= 2 * tolerance + 1;

        return enoughPrecommits && this.enoughPrecommitsCheck;
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
        this.enoughPrecommitsCheck = false;
        Duration duration = Duration.ofSeconds(this.TIMEOUT);
        this.setTimeout("Timeout Precommit", () -> {
            onTimeoutPrecommit(height, round);
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
        log.info("Timeout Precommit called");
        if (this.height == height
                && this.round == round) {
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
        this.messageLog.clear();
    }

    private boolean valid(Block block) {
        // Case 1: Handle NULL_BLOCK explicitly
        if (block == TendermintReplica.NULL_BLOCK) {
            return true; // NULL_BLOCK is always valid
        }

        // Case 2: Handle initial commit (genesis block)
        if (getCommitLog().get((int) this.height) == null) {
            return block.getHashOfLastBlock() == 0; // Empty hashOfLastBlock for genesis is valid
        }

        // Case 3: Validate hashOfLastBlock against the last committed block's hash
        Block lastCommittedBlock = (Block) getCommitLog().get((int) this.height);
        return block.getHashOfLastBlock() == lastCommittedBlock.hashCode();
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
            String clientId = ((DefaultClientRequestPayload) message).getOperation().toString().split("/")[0];
            receiveRequest(sender, new RequestMessage(((DefaultClientRequestPayload) message).getOperation(), System.currentTimeMillis(), clientId));
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
            if (messageLog.fPlus1MessagesInRound(height, round)) {
                GenericMessage m = (GenericMessage) message;
                startRound(m.getRound());
            } else {
                if (message instanceof ProposalMessage) {
                    handleProposal((ProposalMessage) message);
                } else if (message instanceof PrevoteMessage) {
                    handlePrevote((PrevoteMessage) message);
                } else if (message instanceof PrecommitMessage) {
                    handlePrecommit((PrecommitMessage) message);
                } else {
                    log.warning("Unhandled message type: " + message.getType());
                }
            }
        }
    }

    private void handleGossipRequest(GossipRequest message) {
        messageLog.bufferRequest(message.getRequest());
        if (proposer(this.height, this.round).equals(this.getId())) {
            startRound(this.round);
        }
    }

    private void handleGossipMessage(GossipMessage message) {
        if (validateMessage(message.getGossipMessage())) {
            log.warning("Invalid message received from " + message.getReplicaId());
            return;
        }
        if (messageLog.fPlus1MessagesInRound(message.getGossipMessage().getHeight(), round)) {
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
    public void handleClientRequest(String clientId, Serializable request) {
        log.info("Received client request: " + request);
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
        this.step = Step.PROPOSE;
        Block proposal;
        if (proposer(height, round).equals(getId())) {
            if (validValue != null) {
                proposal = validValue;
            } else {
                proposal = getValue();
            }
            if (proposal != null)
                broadcastProposal(height, round, proposal, validRound);
        } else {
            Duration duration = Duration.ofSeconds(this.TIMEOUT);
            this.setTimeout("Timeout Propose", () -> {
                log.info("Timeout Propose called");
                onTimeoutPropose(height, round);
            }, duration);
        }
    }

    private Block getValue() {
        log.info("Getting value");

        // Retrieve the first request message from the message log
        Optional<RequestMessage> requestPayload = messageLog.getRequests().stream().findFirst();

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

        int hashOfLastBlock = getCommitLog().get((int) height) == null
                ? 0
                : getCommitLog().get((int) height).hashCode();

        // Return a Block with the extracted values
        return new Block(
                height,
                round,
                messageLog.getMessageCount() + 1,
                operationString,
                hashOfLastBlock,
                new RequestMessage(payload.getOperation(), requestPayload.get().getTimestamp(), clientId)
        );
    }

    private String proposer(long height, long round) {
        // Generate the proposer list based on voting power
        List<String> proposerList = new ArrayList<>();
        long maxVotingPower = 0;

        // Find the maximum voting power
        for (Map.Entry<String, Integer> entry : votingPower.entrySet()) {
            maxVotingPower = Math.max(maxVotingPower, entry.getValue());
        }

        // Group nodes by their voting power
        Map<Integer, List<String>> powerGroups = new TreeMap<>(Comparator.reverseOrder());
        for (Map.Entry<String, Integer> entry : votingPower.entrySet()) {
            powerGroups.computeIfAbsent(entry.getValue(), k -> new ArrayList<>()).add(entry.getKey());
        }

        // Shuffle groups with the same voting power to randomize tie-breaking
        for (List<String> group : powerGroups.values()) {
            Collections.shuffle(group, rand);
        }

        // Build the proposer list
        for (int i = 0; i < maxVotingPower; i++) {
            for (Map.Entry<Integer, List<String>> group : powerGroups.entrySet()) {
                for (String node : group.getValue()) {
                    if (group.getKey() > i) {
                        proposerList.add(node);
                    }
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
        this.setView(height, proposer(height, round));
    }

    public void sendReply(String clientId, ReplyMessage reply) {
        this.sendReplyToClient(clientId, reply);
    }
}