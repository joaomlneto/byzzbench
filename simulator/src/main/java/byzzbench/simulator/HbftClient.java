package byzzbench.simulator;

import byzzbench.simulator.protocols.hbft.message.*;
import byzzbench.simulator.protocols.hbft.pojo.ClientReplyKey;
import byzzbench.simulator.transport.MessagePayload;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.*;


/**
 * Represents a client in the system. Each client has a unique identifier.
 * The client is responsible for sending requests to the replicas in the system.
 */
@Getter
@SuperBuilder
public class HbftClient extends Client {
    /**
     * The message digest algorithm to use for hashing messages.
     */
    @JsonIgnore
    static MessageDigest md;

    static {
        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * The replies received by the client.
     */
    private final SortedMap<String, SortedMap<ClientReplyKey, Collection<MessagePayload>>> hbftreplies = new TreeMap<>();

    /**
     * The request sequence number already completed.
     */
    private final Set<ClientReplyKey> completedRequests = new HashSet<>();

    /**
     * The sent requests.
     */
    private final SortedMap<Long, RequestMessage> sentRequests = new TreeMap<>();

    /**
     * The sent requests by timestamp.
     */
    private final SortedMap<Long, String> sentRequestsByTimestamp = new TreeMap<>();

    /**
     * timeouts
     */
    private final SortedMap<Long, Long> timeouts = new TreeMap<>();

    /**
     * Timeout for client in seconds
     */
    private final long timeout = 8;


    /**
     * As of hBFT 4.1, sends a request to all replica in the system.
     */
    @Override
    public void sendRequest() {
        String requestId = String.format("%s/%d", super.id, super.requestSequenceNumber.incrementAndGet());
        long timestamp = System.currentTimeMillis();
        RequestMessage request = new RequestMessage(requestId, timestamp, super.id);
        this.sentRequests.put(super.requestSequenceNumber.get(), request);
        this.sentRequestsByTimestamp.put(timestamp, requestId);
        this.broadcastRequest(timestamp, requestId);

        // Set timeout
        Long timeoutId = this.setTimeout("REQUEST", this::retransmitOrPanic, this.timeout);
        timeouts.put(super.requestSequenceNumber.get(), timeoutId);
    }

    public void retransmitOrPanic() {
        long tolerance = (long) Math.floor((super.scenario.getTransport().getNodeIds().size() - 1) / 3);
        if (this.shouldRetransmit(tolerance)) {
            String requestId = String.format("%s/%d", super.id, super.requestSequenceNumber.get());
            // Based on hBFT 4.1 it uses the identical request
            // TODO: It probably should not be the same timestamp
            long timestamp = this.sentRequests.get(super.requestSequenceNumber.get()).getTimestamp();
            this.broadcastRequest(timestamp, requestId);
        } else if (this.shouldPanic(tolerance)) {
            RequestMessage message = this.sentRequests.get(super.requestSequenceNumber.get());
            PanicMessage panic = new PanicMessage(this.digest(message), System.currentTimeMillis(), super.id);
            super.scenario.getTransport().multicast(this, super.scenario.getTransport().getNodeIds(), panic);
        }
        this.clearTimeout(timeouts.get(super.requestSequenceNumber.get()));
        Long timeoutId = this.setTimeout("REQUEST", this::retransmitOrPanic, this.timeout);
        timeouts.put(super.requestSequenceNumber.get(), timeoutId);
    }

    private void broadcastRequest(long timestamp, String requestId) {
        MessagePayload payload = new ClientRequestMessage(timestamp, requestId);
        SortedSet<String> replicaIds = super.scenario.getTransport().getNodeIds();
        getScenario().getTransport().multicast(this, replicaIds, payload);
    }

    /**
     * Handles a reply received by the client.
     *
     * @param senderId The ID of the sender of the reply.
     * @param payload  The payload received by the client.
     */
    public void handleMessage(String senderId, MessagePayload payload) {
        if (!(payload instanceof ClientReplyMessage clientReplyMessage)) {
            return;
        }
        ReplyMessage reply = clientReplyMessage.getReply();
        ClientReplyKey key = new ClientReplyKey(reply.getResult().toString(), reply.getSequenceNumber());
        // Default is for testing only
        String currRequest = this.sentRequestsByTimestamp.getOrDefault(reply.getTimestamp(), "C/0");
        this.hbftreplies.putIfAbsent(currRequest, new TreeMap<>());
        this.hbftreplies.get(currRequest).putIfAbsent(key, new ArrayList<>());
        this.hbftreplies.get(currRequest).get(key).add(reply);

        /**
         * If the client received 2f + 1 correct replies,
         * and the request has not been completed yet.
         */
        if (this.completedReplies(clientReplyMessage.getTolerance())
                && !this.completedRequests.contains(key)
                && super.requestSequenceNumber.get() <= this.maxRequests) {
            this.completedRequests.add(key);
            this.clearTimeout(this.timeouts.get(super.requestSequenceNumber.get()));
            this.sendRequest();
        }
    }

    /**
     * Set a timeout for this replica.
     *
     * @param name    a name for the timeout
     * @param r       the runnable to execute when the timeout occurs
     * @param timeout the timeout duration
     * @return the timer object
     */
    public long setTimeout(String name, Runnable r, long timeout) {
        Duration duration = Duration.ofSeconds(timeout);
        return super.scenario.getTransport().setTimeout(this, r, duration, name);
    }

    /**
     * Checks whether client should retransmit the request
     * if #replies < f + 1
     */
    public boolean shouldRetransmit(long tolerance) {
        String currRequest = String.format("%s/%d", super.id, super.requestSequenceNumber.get());
        if (!hbftreplies.containsKey(currRequest)) {
            return true;
        }
        for (ClientReplyKey key : hbftreplies.get(currRequest).keySet()) {
            return !(this.hbftreplies.get(currRequest).get(key).size() >= tolerance + 1);
        }
        return true;
    }

    /**
     * Checks whether client should send PANIC
     * if f + 1 <= #replies < 2f + 1
     */
    public boolean shouldPanic(long tolerance) {
        String currRequest = String.format("%s/%d", super.id, super.requestSequenceNumber.get());
        for (ClientReplyKey key : hbftreplies.get(currRequest).keySet()) {
            return this.hbftreplies.get(currRequest).get(key).size() >= tolerance + 1
                    && this.hbftreplies.get(currRequest).get(key).size() < tolerance * 2 + 1;
        }
        return false;
    }

    /**
     * Checks whether it has received 2f + 1 replies
     */
    public boolean completedReplies(long tolerance) {
        String currRequest = String.format("%s/%d", super.id, super.requestSequenceNumber.get());
        if (!hbftreplies.containsKey(currRequest)) {
            return false;
        }
        for (ClientReplyKey key : hbftreplies.get(currRequest).keySet()) {
            if (this.hbftreplies.get(currRequest).get(key).size() >= 2 * tolerance + 1) {
                return true;
            }
        }
        return false;
    }

    /**
     * Clear all timeouts for this client.
     */
    // public void clearAllTimeouts() {
    //     super.scenario.getTransport().clearClientTimeouts(super.id);
    // }

    /**
     * Create a digest of a message.
     *
     * @param message the message to digest
     * @return the digest of the message
     */
    public byte[] digest(Serializable message) {
        return md.digest(message.toString().getBytes());
    }

}
