package byzzbench.simulator.exploration_strategy.byzzfuzz;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.faults.Fault;
import byzzbench.simulator.nodes.Node;
import byzzbench.simulator.transport.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;

/**
 * Oracle for extracting round information from messages as per the
 * <a href="https://dl.acm.org/doi/10.1145/3586053">ByzzFuzz algorithm</a>
 */
@Getter
public abstract class ByzzFuzzRoundInfoOracle implements Serializable, TransportObserver, ScenarioObserver {
    @JsonIgnore
    private final Scenario scenario;
    private final Map<String, ByzzFuzzRoundInfo> replicasRoundInfo = new HashMap<>();
    private final Map<String, Long> replicaRounds = new HashMap<>();
    private final Map<Long, Long> messageRound = new HashMap<>();

    /**
     * Create a new ByzzFuzzRoundInfoOracle for the given scenario
     *
     * @param scenario The scenario
     */
    public ByzzFuzzRoundInfoOracle(Scenario scenario) {
        this.scenario = scenario;
        scenario.getTransport().addObserver(this);
    }

    /**
     * Get the current estimated round number for the given replica
     *
     * @param replicaId The replica ID
     * @return The current round number
     */
    public long getReplicaRound(String replicaId) {
        return this.replicaRounds.computeIfAbsent(replicaId, id -> 0L);
    }

    public ByzzFuzzRoundInfo getReplicaRoundInfo(String replicaId) {
        return this.replicasRoundInfo.computeIfAbsent(replicaId, id -> new ByzzFuzzRoundInfo(0, 0, 0));
    }

    /**
     * Update the round information for the given replica based on the received message round info
     *
     * @param replicaId        The replica ID
     * @param messageRoundInfo The message round info
     */
    public void updateReplicaRoundInfo(String replicaId, ByzzFuzzRoundInfo messageRoundInfo) {
        ByzzFuzzRoundInfo replicaRoundInfo = this.getReplicaRoundInfo(replicaId);
        if (messageRoundInfo.getViewNumber() > replicaRoundInfo.getViewNumber()) {
            // timeout / new-view triggered, possibly faulty leader, move to the next round
            this.replicasRoundInfo.put(replicaId, messageRoundInfo);
        } else if (messageRoundInfo.getViewNumber() == replicaRoundInfo.getViewNumber()
                && messageRoundInfo.getSequenceNumber() > replicaRoundInfo.getSequenceNumber()) {
            // sequence number incremented (something was committed)
            this.replicasRoundInfo.put(replicaId, messageRoundInfo);
        } else if (messageRoundInfo.getViewNumber() == replicaRoundInfo.getViewNumber()
                && messageRoundInfo.getSequenceNumber() == replicaRoundInfo.getSequenceNumber()
                && messageRoundInfo.getVerbIndex() > replicaRoundInfo.getVerbIndex()) {
            // increase the round until the final verb (possibly out-of-order messages)
            this.replicasRoundInfo.put(replicaId, messageRoundInfo);
        }
    }

    /**
     * Compute the difference in rounds between the given replica and the message round info
     *
     * @param replicaId        the replica ID
     * @param messageRoundInfo the round info from the message
     * @return >= 0 if replicaId is at a higher round or equal round than messageRoundInfo, < 0 otherwise
     */
    private int inSmallerRound(String replicaId, ByzzFuzzRoundInfo messageRoundInfo) {
        ByzzFuzzRoundInfo replicaRoundInfo = this.getReplicaRoundInfo(replicaId);

        // Timeout/new-view triggered, possibly faulty leader, move to the next round
        if (messageRoundInfo.getViewNumber() > replicaRoundInfo.getViewNumber()) {
            return 1;
        }

        // check if the sequence number incremented (something committed)
        // possibly out of order protocol messages
        if (messageRoundInfo.getViewNumber() == replicaRoundInfo.getViewNumber()
                && messageRoundInfo.getSequenceNumber() > replicaRoundInfo.getSequenceNumber()) {
            this.replicasRoundInfo.put(replicaId, messageRoundInfo);
            return messageRoundInfo.getVerbIndex() + numRoundsToProcessRequest() - replicaRoundInfo.getVerbIndex();
        }

        // check if the same sequence number but higher verb index
        if (messageRoundInfo.getViewNumber() == replicaRoundInfo.getViewNumber()
                && messageRoundInfo.getSequenceNumber() == replicaRoundInfo.getSequenceNumber()
                && messageRoundInfo.getVerbIndex() > replicaRoundInfo.getVerbIndex()) {
            this.replicasRoundInfo.put(replicaId, messageRoundInfo);
            return messageRoundInfo.getVerbIndex() - replicaRoundInfo.getVerbIndex();
        }

        return 0;
    }

    /**
     * Check if the given node ID corresponds to a Client
     *
     * @param nodeId The node ID
     * @return True if the node is a Client, false otherwise
     */
    private boolean isClient(String nodeId) {
        return scenario.getClients().containsKey(nodeId);
    }

    /**
     * Check if the given node ID corresponds to a Replica
     *
     * @param nodeId The node ID
     * @return True if the node is a Replica, false otherwise
     */
    private boolean isReplica(String nodeId) {
        return scenario.getReplicas().containsKey(nodeId);
    }

    @Override
    public void onMulticast(Node sender, SortedSet<String> recipients, MessagePayload payload) {
        // ensure the message is MessageWithByzzFuzzRoundInfo
        if (!(payload instanceof MessageWithByzzFuzzRoundInfo messageWithRoundInfo)) {
            return;
        }
        String senderReplicaId = sender.getId();

        // ensure the sender is a replica
        if (!isReplica(senderReplicaId)) {
            return;
        }

        ByzzFuzzRoundInfo messageRoundInfo = extractMessageRoundInformation(messageWithRoundInfo);
        long currentReplicaRound = getReplicaRound(senderReplicaId);

        // check if the recipients are replicas or clients
        boolean sentToClient = recipients.stream().anyMatch(this::isClient);
        boolean sentToReplicas = recipients.stream().anyMatch(this::isReplica);

        // if both are true... something went wrong
        if (sentToClient && sentToReplicas) {
            throw new IllegalStateException("Message sent to both clients and replicas?!");
        }

        // if both are false... something went wrong
        if (!sentToClient && !sentToReplicas) {
            throw new IllegalStateException("Message sent to neither clients nor replicas?!");
        }

        // Replica multicasted a message to other replicas
        if (sentToReplicas) {
            // if verb index is zero, skip
            if (messageRoundInfo.getVerbIndex() == 0) {
                return;
            }
            this.replicaRounds.put(senderReplicaId, currentReplicaRound + inSmallerRound(senderReplicaId, messageRoundInfo));
            this.updateReplicaRoundInfo(senderReplicaId, messageRoundInfo);
            return;
        }

        // Replica sent a message to clients
        if (sentToClient) {
            this.replicaRounds.put(senderReplicaId, currentReplicaRound + 1);
            ByzzFuzzRoundInfo replicaRoundInfo = this.getReplicaRoundInfo(senderReplicaId);
            ByzzFuzzRoundInfo afterReplyRoundInfo = new ByzzFuzzRoundInfo(
                    replicaRoundInfo.getViewNumber(),
                    replicaRoundInfo.getSequenceNumber(),
                    this.numRoundsToProcessRequest()
            );
            this.replicasRoundInfo.put(senderReplicaId, afterReplyRoundInfo);
        }
    }

    /**
     * Extract the round information from the given message
     *
     * @param messageWithRoundInfo The message with round info
     * @return The extracted round information
     */
    public ByzzFuzzRoundInfo extractMessageRoundInformation(MessageWithByzzFuzzRoundInfo messageWithRoundInfo) {
        return new ByzzFuzzRoundInfo(
                messageWithRoundInfo.getViewNumber(),
                messageWithRoundInfo.getRound(),
                getProtocolMessageVerbIndex(messageWithRoundInfo)
        );
    }

    @Override
    public void onEventAdded(Event event) {
        // Check if it is a message
        if (!(event instanceof MessageEvent messageEvent)) {
            return;
        }

        // check if it is a message with round
        if (!(messageEvent.getPayload() instanceof MessageWithByzzFuzzRoundInfo messageWithRoundInfo)) {
            return;
        }

        // tag message with a round
        this.messageRound.put(event.getEventId(), this.replicaRounds.get(messageEvent.getSenderId()));
    }

    @Override
    public void onEventDropped(Event event) {
        // Do nothing
    }

    @Override
    public void onEventRequeued(Event event) {
        // Do nothing
    }

    @Override
    public void onEventDelivered(Event event) {
        // ensure it is a message
        if (!(event instanceof MessageEvent messageEvent)) {
            return;
        }

        // ensure it has byzzfuzz round information
        if (!(event instanceof MessageWithByzzFuzzRoundInfo messageWithRoundInfo)) {
            return;
        }

        ByzzFuzzRoundInfo messageRoundInfo = extractMessageRoundInformation(messageWithRoundInfo);
        if (messageRoundInfo.getVerbIndex() == 0) {
            // do nothing
            return;
        }

        String receiverId = messageEvent.getRecipientId();
        long currentReplicaRound = getReplicaRound(receiverId);
        this.replicaRounds.put(receiverId, currentReplicaRound + inSmallerRound(receiverId, messageRoundInfo));
        this.updateReplicaRoundInfo(receiverId, messageRoundInfo);
    }

    @Override
    public void onMessageMutation(MutateMessageEventPayload payload) {
        // Do nothing
    }

    @Override
    public void onFault(Fault fault) {
        // Do nothing
    }

    @Override
    public void onTimeout(TimeoutEvent event) {
        // Do nothing
    }

    @Override
    public void onGlobalStabilizationTime() {
        // Do nothing
    }

    /**
     * Get the protocol message verb index for the given message.
     * If the returned value is zero, the message is considered not part of the protocol rounds.
     *
     * @param message The message.
     * @return The protocol message verb index.
     */
    public abstract int getProtocolMessageVerbIndex(MessageWithByzzFuzzRoundInfo message);

    /**
     * Return how many rounds (in the happy path) are required to process a request
     *
     * @return The number of rounds
     */
    public abstract int numRoundsToProcessRequest();

    @Override
    public void onReplicaAdded(Replica replica) {
        this.replicasRoundInfo.put(replica.getId(), new ByzzFuzzRoundInfo(0, 0, 0));
        this.replicaRounds.put(replica.getId(), 0L);
    }

    @Override
    public void onClientAdded(Client client) {
        // do nothing
    }
}
