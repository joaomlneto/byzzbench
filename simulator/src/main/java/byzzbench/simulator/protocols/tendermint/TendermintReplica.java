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

    private MessageLog messageLog;

    private final long tolerance = 1;

    // Assigned powers of each replica in the network
    private final Map<String, Integer> votingPower = new HashMap<>();

    private boolean precommitRule0Check;
    private boolean prevoteRule1Check;
    private boolean prevoteRule2Check;

    public static final Block NULL_BLOCK = new Block(Long.MIN_VALUE, "NULL VALUE", null);

    public final int TIMEOUT = 50;

    public Random rand = new Random(2137L);

    private SortedSet<Pair<Long, Long>> hasBroadcasted = new TreeSet<>();


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
        this.precommitRule0Check = true;
        this.prevoteRule1Check = true;
        this.prevoteRule2Check = true;
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
        // upon ⟨PROPOSAL, hp, roundp, v,−1⟩ from proposer(hp, roundp)
        boolean proposalExists = messageLog.getProposals().getOrDefault(block, new ArrayList<>()).stream()
                .filter(proposal -> proposal.getHeight() == height)
                .filter(proposal -> proposal.getRound() == round)
                .filter(proposal -> proposal.getReplicaId().equals(proposer(height, round)))
                .filter(proposal -> proposal.getValidRound() == -1)
                .count() >= 1;

        // while stepp = propose
        boolean b = this.step == Step.PROPOSE;

        return proposalExists && b;
    }

    private boolean fulfillProposalRule1(ProposalMessage proposalMessage) {
        // upon ⟨PROPOSAL, hp, roundp, v, vr⟩ from proposer(hp, roundp)
        boolean proposalExists = messageLog.getProposals()
                .getOrDefault(proposalMessage.getBlock(), new ArrayList<>()).stream()
                .filter(proposal -> proposal.getHeight() == height)
                .filter(proposal -> proposal.getRound() == round)
                .anyMatch(proposal -> proposal.getReplicaId().equals(proposer(height, round)));

        // AND 2f + 1 ⟨PREVOTE, hp, vr, id(v)⟩
        boolean enoughPrevotes = messageLog.getPrevotes().getOrDefault(proposalMessage.getBlock(), new ArrayList<>()).stream()
                .filter(prevote -> prevote.getHeight() == height)
                .filter(prevote -> prevote.getRound() == proposalMessage.getValidRound())
                .count() >= 2 * tolerance + 1;

        // stepp = propose ∧ (vr ≥ 0 ∧ vr < roundp)
        boolean b = this.step == Step.PROPOSE && proposalMessage.getValidRound() >= 0 && proposalMessage.getValidRound() < this.round;

        return proposalExists && enoughPrevotes && b;
    }

    private boolean fulfillProposalRule2(ProposalMessage proposalMessage) {
        // upon ⟨PROPOSAL, hp, roundp, v, ∗⟩ from proposer(hp, roundp)
        boolean proposalExists = messageLog.getProposals().getOrDefault(proposalMessage.getBlock(), new ArrayList<>()).stream()
                .filter(proposal -> proposal.getHeight() == height)
                .filter(proposal -> proposal.getRound() == round)
                .filter(proposal -> proposal.getReplicaId().equals(proposer(height, round)))
                .count() >= 1;

        // 2f + 1 ⟨PREVOTE, hp, roundp, id(v)⟩
        boolean enoughPrevotes = messageLog.getPrevotes().getOrDefault(proposalMessage.getBlock(), new ArrayList<>()).stream()
                .filter(prevote -> prevote.getHeight() == height)
                .filter(prevote -> prevote.getRound() == round)
                .count() >= 2 * tolerance + 1;

        // valid(v) ∧ stepp ≥ prevote for the first time do
        boolean b = valid(proposalMessage.getBlock())
                && (this.step == Step.PREVOTE || this.step == Step.PRECOMMIT);

        return proposalExists && enoughPrevotes && b && this.prevoteRule2Check;
    }

    private boolean fulfillProposalRule3(ProposalMessage proposalMessage) {
        // upon ⟨PROPOSAL, hp, r, v, ∗⟩ from proposer(hp, r)
        boolean proposalExists = messageLog.getProposals().getOrDefault(proposalMessage.getBlock(), new ArrayList<>()).stream()
                .filter(proposal -> proposal.getHeight() == height)
                .filter(proposal -> proposal.getRound() == proposalMessage.getRound())
                .filter(proposal -> proposal.getRound() == this.round)
                .filter(proposal -> proposal.getReplicaId().equals(proposer(height, proposalMessage.getRound())))
                .count() >= 1;

        // AND 2f + 1 ⟨PRECOMMIT, hp, r, id(v)⟩
        boolean enoughPrecommits = messageLog.getPrecommits().getOrDefault(proposalMessage.getBlock(), new ArrayList<>()).stream()
                .filter(precommit -> precommit.getHeight() == height)
                .filter(precommit -> precommit.getRound() == proposalMessage.getRound())
                .filter(precommit -> precommit.getRound() == this.round)
                .count() >= 2 * tolerance + 1;

        // while decisionp[hp] = nil
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
            return;
        }

        // Shuffle the indices to randomize the execution order
        Collections.shuffle(trueIndices, rand);


        // Execute each rule in the randomized order
        for (int index : trueIndices) {
            switch (index) {
                case 0:
                    executeProposalRule(proposalMessage);
                    break;
                case 1:
                    executeProposalPrevoteRule1(proposalMessage.getBlock(), proposalMessage.getValidRound());
                    break;
                case 2:
                    executeProposalPrevoteRule2(proposalMessage.getBlock());
                    break;
                case 3:
                    executeProposalPrecommitRule(proposalMessage.getBlock());
                    break;
                default:
                    log.warning("Unexpected index: " + index);
                    break;
            }
        }
    }

    private void executeProposalRule(ProposalMessage proposalMessage) {

        // if valid(v) ∧ (lockedRoundp = −1 ∨ lockedValuep = v) then
        if (valid(proposalMessage.getBlock()) &&
                (lockedRound == -1 || lockedValue.equals(proposalMessage.getBlock()))) {
            //24: broadcast ⟨PREVOTE, hp, roundp, id(v)⟩
            broadcastPrevote(height, round, proposalMessage.getBlock());
        } else {
            //26: broadcast ⟨PREVOTE, hp, roundp, nil⟩
            broadcastPrevote(height, round, NULL_BLOCK);
        }
        //27: stepp ← prevote
        this.step = Step.PREVOTE;
    }

    private void executeProposalPrevoteRule1(Block block, long validRound) {
        // if valid(v) ∧ (lockedRoundp ≤ vr ∨ lockedValuep = v) then
        if (valid(block)
                && (lockedRound <= validRound || lockedValue.equals(block))) {
            // 30: broadcast ⟨PREVOTE, hp, roundp, id(v)⟩
            broadcastPrevote(height, round, block);
        } else {
            // 32: broadcast ⟨PREVOTE, hp, roundp, nil⟩
            broadcastPrevote(height, round, NULL_BLOCK);
        }
        // 33: stepp ← prevote
        this.step = Step.PREVOTE;
    }

    private void executeProposalPrevoteRule2(Block block) {
        // Execution logic
        prevoteRule2Check = false;
        // if stepp = prevote then
        if (this.step == Step.PREVOTE) {
            // lockedValuep ← v
            //39: lockedRoundp ← roundp
            //40: broadcast ⟨PRECOMMIT, hp, roundp, id(v))⟩
            //41: stepp ← precommit
            lockedValue = block;
            lockedRound = this.round;
            broadcastPrecommit(height, round, block);
            this.step = Step.PRECOMMIT;
        }
        // 42: validV aluep ← v
        //43: validRoundp ← roundp
        validValue = block;
        validRound = this.round;
    }

    private void executeProposalPrecommitRule(Block block) {
        // 50: if valid(v) then
        if (valid(block)) {
            // 51: decisionp[hp] = v
            //52: hp ← hp + 1
            //53: reset lockedRoundp, lockedV aluep, validRoundp and validV aluep to initial values and empty message log
            //54: StartRound(0)
            commitOperation(block);
            ReplyMessage replyMessage = new ReplyMessage(
                    this.height,
                    block.getRequestMessage().getTimestamp(),
                    block.getRequestMessage().getClientId(),
                    this.getId(),
                    block.getRequestMessage().getOperation());
            sendReply(block.getRequestMessage().getClientId(), replyMessage);
            height++;
            messageLog.removeRequest(block);
            reset(block);
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
        ProposalMessage proposalMessage = new ProposalMessage(getId(), height, round, validRound, proposal);
        broadcastMessageIncludingSelf(proposalMessage);
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
        // 2f + 1 ⟨PREVOTE, hp, vr, id(v)⟩
        boolean enoughPrevotes = messageLog.getPrevotes()
                .getOrDefault(prevoteMessage.getBlock(), new ArrayList<>()).stream()
                .filter(prevote -> prevote.getHeight() == height)
                .filter(prevote -> prevote.getRound() == prevoteMessage.getRound())
                .count() >= 2 * tolerance + 1;

        // ⟨PROPOSAL, hp, roundp, v, vr⟩ from proposer(hp, roundp)
        boolean hasMatchingProposal = messageLog.getProposals()
                .getOrDefault(prevoteMessage.getBlock(), new ArrayList<>()).stream()
                .filter(proposal -> proposal.getHeight() == this.height)
                .filter(proposal -> proposal.getRound() == this.round)
                .filter(proposal -> proposal.getReplicaId().equals(proposer(this.height, this.round)))
                .anyMatch(proposal -> proposal.getValidRound() == prevoteMessage.getRound());

        // stepp = propose ∧ (vr ≥ 0 ∧ vr < roundp)
        boolean b = this.step == Step.PROPOSE && prevoteMessage.getRound() >= 0 && prevoteMessage.getRound() < this.round;

        return enoughPrevotes && hasMatchingProposal && b;
    }

    private boolean fulfillPrevoteRule1(PrevoteMessage prevoteMessage) {
        boolean enoughPrevotes = messageLog.getPrevotes().getOrDefault(prevoteMessage.getBlock(), new ArrayList<>()).stream()
                .filter(prevote -> prevote.getHeight() == this.height)
                .filter(prevote -> prevote.getRound() == this.round)
                .count() >= 2 * tolerance + 1;

        boolean rest = this.step == Step.PREVOTE;

        return enoughPrevotes && rest && this.prevoteRule1Check;
    }

    private boolean fulfillPrevoteRule2(PrevoteMessage prevoteMessage) {
        boolean enoughPrevotes = messageLog.getPrevotes().getOrDefault(prevoteMessage.getBlock(), new ArrayList<>()).stream()
                .filter(prevote -> prevote.getHeight() == height)
                .filter(prevote -> prevote.getRound() == round)
                .count() >= 2 * tolerance + 1;

        boolean proposalExists = messageLog.getProposals().getOrDefault(prevoteMessage.getBlock(), new ArrayList<>()).stream()
                .filter(proposal -> proposal.getHeight() == height)
                .filter(proposal -> proposal.getRound() == round)
                .filter(proposal -> proposal.getReplicaId().equals(proposer(height, round)))
                .count() >= 1;

        boolean b = valid(prevoteMessage.getBlock())
                && (this.step == Step.PREVOTE || this.step == Step.PRECOMMIT);

        return enoughPrevotes && proposalExists && b && this.prevoteRule2Check;
    }

    private boolean fulfillPrevoteRule3() {
        boolean enoughPrevotes = messageLog.getPrevotes().getOrDefault(NULL_BLOCK, new ArrayList<>()).stream()
                .filter(prevote -> prevote.getHeight() == height)
                .filter(prevote -> prevote.getRound() == round)
                .count() >= 2 * tolerance + 1;
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
            return;
        }

        // Shuffle the indices to randomize the execution order
        Collections.shuffle(trueIndices, rand);


        // Execute each rule in the randomized order
        for (int index : trueIndices) {
            switch (index) {
                case 0:
                    executeProposalPrevoteRule1(prevoteMessage.getBlock(), prevoteMessage.getRound());
                    break;
                case 1:
                    executePrevoteRule1();
                    break;
                case 2:
                    executeProposalPrevoteRule2(prevoteMessage.getBlock());
                    break;
                case 3:
                    executePrevoteRule3();
                    break;
                default:
                    log.warning("Unexpected index: " + index);
                    break;
            }
        }
    }

    private void executePrevoteRule1() {
        this.prevoteRule1Check = false;
        Duration duration = Duration.ofSeconds(this.TIMEOUT);
        this.setTimeout("Timeout Prevote", () -> {
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
        boolean enoughPrecommits = messageLog.getPrecommits().getOrDefault(precommitMessage.getBlock(), new ArrayList<>()).stream()
                .filter(precommit -> precommit.getHeight() == this.height)
                .filter(precommit -> precommit.getRound() == this.round)
                .count() >= 2 * tolerance + 1;

        boolean proposalExists = messageLog.getProposals().getOrDefault(precommitMessage.getBlock(), new ArrayList<>()).stream()
                .filter(proposal -> proposal.getHeight() == this.getHeight())
                .filter(proposal -> proposal.getRound() == precommitMessage.getRound())
                .filter(proposal -> proposal.getRound() == this.round)
                .filter(proposal -> proposal.getReplicaId().equals(proposer(precommitMessage.getHeight(), precommitMessage.getRound())))
                .count() >= 1;

        boolean decisionIsNull = getCommitLog().get((int) precommitMessage.getHeight()) == null;

        return enoughPrecommits && proposalExists && decisionIsNull;
    }

    private boolean fulfillPrecommitRule() {
        boolean enoughPrecommits = messageLog.getPrecommits().values().stream()
                .flatMap(List::stream)  // Flatten the lists into a single stream
                .toList().stream()
                .filter(precommitMessage -> precommitMessage.getHeight() == height)
                .filter(precommitMessage -> precommitMessage.getRound() == round)
                .count() >= 2 * tolerance + 1;

        return enoughPrecommits && this.precommitRule0Check;
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
            return;
        }

        // Shuffle the indices to randomize the execution order
        Collections.shuffle(trueIndices, rand);


        // Execute each rule in the randomized order
        for (int index : trueIndices) {
            switch (index) {
                case 0:
                    executePrecommitRule0();
                    break;
                case 1:
                    executeProposalPrecommitRule(block);
                    break;
                default:
                    log.warning("Unexpected index: " + index);
                    break;
            }
        }
    }

    private void executePrecommitRule0() {
        this.precommitRule0Check = false;
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
        if (this.height == height
                && this.round == round) {
            startRound(this.round + 1);
        }
        else
            log.info("Timeout Precommit called but height and round do not match");
    }

    /**
     * MISCELLANEOUS
     */

    private void reset(Block block) {
        this.lockedValue = null;
        this.lockedRound = -1;
        this.validValue = null;
        this.validRound = -1;
//        this.messageLog.clear(block);
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
            if (((GenericMessage) message).getHeight() < height || ((GenericMessage) message).getRound() < round) {
                log.warning("Message height or round mismatch: " + ((GenericMessage) message).getHeight());
                return true;
            }
            if (((GenericMessage) message).getAuthor().equals(this.getId())) {
                if (message instanceof ProposalMessage ) {
                    log.warning("Can accept proposals from itself: " + ((GenericMessage) message).getRound());
                    return false;
                }
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
            if (messageLog.fPlus1MessagesInRound(height, ((GenericMessage) message).getRound())) {
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
        this.setView(height, roundNumber);
        this.round = roundNumber;
        this.step = Step.PROPOSE;
        this.precommitRule0Check = true;
        this.prevoteRule1Check = true;
        this.prevoteRule2Check = true;
        Block proposal;
        if (Objects.equals(proposer(height, round), this.getId())) {
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
                onTimeoutPropose(height, round);
            }, duration);
        }
    }

    private Block getValue() {
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

        // Return a Block with the extracted values
        return new Block(
                messageLog.getMessageCount() + 1,
                operationString,
                new RequestMessage(payload.getOperation(), requestPayload.get().getTimestamp(), clientId)
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
        if(index < 0) {
            index = 0;
        }
        return proposerList.get(index);
    }

    @Override
    public void initialize() {
        startRound(0);
        setView(0, 0);
    }

    public void setView(long height, long round) {
        this.setView(height, proposer(height, round));
    }

    public void sendReply(String clientId, ReplyMessage reply) {
        this.sendReplyToClient(clientId, reply);
    }

    public void print() {
        // print all the attributes of the replica
        log.info("Replica ID: " + this.getId());
        log.info("Height: " + this.height);
        log.info("Round: " + this.round);
        log.info("Step: " + this.step);
        log.info("Locked Value: " + this.lockedValue);
        log.info("Locked Round: " + this.lockedRound);
        log.info("Valid Value: " + this.validValue);
        log.info("Valid Round: " + this.validRound);;
        log.info("Tolerance: " + this.tolerance);

    }
}