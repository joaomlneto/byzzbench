package byzzbench.simulator;

import byzzbench.simulator.protocols.hbft.message.PanicMessage;
import byzzbench.simulator.protocols.hbft.message.RequestMessage;
import byzzbench.simulator.transport.MessagePayload;
import byzzbench.simulator.utils.NonNull;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;

import byzzbench.simulator.protocols.hbft.message.ReplyMessage;
import byzzbench.simulator.protocols.hbft.pojo.ClientReplyKey;


/**
 * Represents a client in the system. Each client has a unique identifier.
 * The client is responsible for sending requests to the replicas in the system.
 */
@Getter
@Builder
@RequiredArgsConstructor
public class Client implements Serializable, Node {
    /**
     * The scenario object that this client belongs to.
     */
    @JsonIgnore
    @NonNull
    private final transient Scenario scenario;


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
     * The unique ID of the client.
     */
    @NonNull
    private final String id;

    /**
     * The sequence number of the next request to be sent by the client.
     */
    private final AtomicLong requestSequenceNumber = new AtomicLong(0);

    /**
     * The maximum number of requests that can be sent by the client.
     */
    private final long maxRequests = 100;

    /**
     * The replies received by the client.
     */
    private final SortedMap<String, SortedMap<ClientReplyKey, Collection<MessagePayload>>> replies = new TreeMap<>();

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


    @Override
    public void initialize() {
        // Send the first request
        this.sendRequest();
        //System.out.println("CLIENT TIMEOUT SETUP");
        //this.setTimeout("sendRequest", this::sendRequest, Duration.ofSeconds(1));
    }

    // /**
    //  * Sends a request to a replica in the system.
    //  */
    // public void sendRequest() {
    //     String recipientId = this.getScenario().getNodes().keySet().iterator().next();
    //     String requestId = String.format("%s/%d", this.id, this.requestSequenceNumber.getAndIncrement());
    //     this.getScenario().getTransport().sendClientRequest(this.id, requestId, recipientId);
    // }

    /**
     * As of hBFT 4.1, sends a request to all replica in the system.
     */
    public void sendRequest() {
        String requestId = String.format("%s/%d", this.id, this.requestSequenceNumber.incrementAndGet());
        long timestamp = System.currentTimeMillis();
        RequestMessage request = new RequestMessage(requestId, timestamp, this.id);
        this.sentRequests.put(this.requestSequenceNumber.get(), request);
        this.sentRequestsByTimestamp.put(timestamp, requestId);
        this.getScenario().getTransport().multicastClientRequest(this.id, timestamp, requestId, this.scenario.getTransport().getNodeIds());

        // Set timeout
        Long timeoutId = this.setTimeout("REQUEST", this::retransmitOrPanic, this.timeout);
        timeouts.put(this.requestSequenceNumber.get(), timeoutId);
    }

    public void retransmitOrPanic() {
        long tolerance = (long) Math.floor((this.scenario.getTransport().getNodeIds().size() - 1) / 3);
        if (this.shouldRetransmit(tolerance)) {
            String requestId = String.format("%s/%d", this.id, this.requestSequenceNumber.get());
            // Based on hBFT 4.1 it uses the identical request
            // TODO: It probably should not be the same timestamp
            long timestamp = this.sentRequests.get(this.requestSequenceNumber.get()).getTimestamp();
            this.scenario.getTransport().multicastClientRequest(this.id, timestamp, requestId, this.scenario.getTransport().getNodeIds());
        } else if (this.shouldPanic(tolerance)) {
            RequestMessage message = this.sentRequests.get(this.requestSequenceNumber.get());
            PanicMessage panic = new PanicMessage(this.digest(message), System.currentTimeMillis(), this.id);
            this.scenario.getTransport().multicast(this.id, this.scenario.getTransport().getNodeIds(), panic);
        }
        this.clearTimeout(timeouts.get(this.requestSequenceNumber.get()));
        Long timeoutId = this.setTimeout("REQUEST", this::retransmitOrPanic, this.timeout);
        timeouts.put(this.requestSequenceNumber.get(), timeoutId);
    }

    /**
     * Handles a reply received by the client.
     * @param senderId The ID of the sender of the reply.
     * @param reply The reply received by the client.
     * @param tolerance the tolerance of the protocol (used for hbft)
     */
    public void handleReply(String senderId, MessagePayload reply, long tolerance, long seqNumber) {
        if (!(reply instanceof ReplyMessage)) {
            return;
        }
        ClientReplyKey key = new ClientReplyKey(((ReplyMessage) reply).getResult().toString(), seqNumber);
        // Default is for testing only
        String currRequest = this.sentRequestsByTimestamp.getOrDefault(((ReplyMessage) reply).getTimestamp(), "C/0");
        this.replies.putIfAbsent(currRequest, new TreeMap<>());
        this.replies.get(currRequest).putIfAbsent(key, new ArrayList<>());
        this.replies.get(currRequest).get(key).add(reply);

        /**
         * If the client received 2f + 1 correct replies,
         * and the request has not been completed yet.
         */
        if (this.completedReplies(tolerance) 
            && !this.completedRequests.contains(key) 
            && this.requestSequenceNumber.get() <= this.maxRequests) {
                this.completedRequests.add(key);
                this.clearTimeout(this.timeouts.get(this.requestSequenceNumber.get()));
                this.sendRequest();
        }
    }

     /**
     * Handles a reply received by the client.
     *
     * @param senderId The ID of the sender of the reply.
     * @param reply    The reply received by the client.
     */
    public void handleMessage(String senderId, MessagePayload reply) {
        // this.replies.putIfAbsent(this.requestSequenceNumber.get(), new ArrayList<>());
        // this.replies.get(this.requestSequenceNumber.get()).add(reply);
        // if (this.requestSequenceNumber.get() < this.maxRequests) {
        //     this.sendRequest();
        // }
    }

    @Override
    public Instant getCurrentTime() {
        return this.scenario.getTimekeeper().getTime(this);
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
        return this.scenario.getTransport().setTimeout(this, r, duration);
    }

        /**
     * Set a timeout for this client.
     *
     * @param r       the runnable to execute when the timeout occurs
     * @param timeout the timeout in milliseconds
     * @return the timeout ID
     */
    public long setTimeout(Runnable r, long timeout) {
        Runnable wrapper = () -> {
            r.run();
        };
        Duration duration = Duration.ofSeconds(timeout);
        return this.scenario.getTransport().setClientTimeout(this.id, wrapper, duration);
    }

    /**
     * Clear a timeout for this replica.
     *
     * @param eventId the event ID of the timeout to clear
     */
    public void clearTimeout(long eventId) {
        this.scenario.getTransport().clearTimeout(this, eventId);
    }

    /**
     * Clear all timeouts for this replica.
     */
    public void clearAllTimeouts() {
        this.scenario.getTransport().clearReplicaTimeouts(this);
    }

    /**
     * Checks whether client should retransmit the request
     * if #replies < f + 1
     */
    public boolean shouldRetransmit(long tolerance) {
        String currRequest = String.format("%s/%d", this.id, this.requestSequenceNumber.get());
        if (!replies.containsKey(currRequest)) {
            return true;
        }
        for (ClientReplyKey key : replies.get(currRequest).keySet()) {
            return !(this.replies.get(currRequest).get(key).size() >= tolerance + 1);
        }
        return true;
    }

    /**
     * Checks whether client should send PANIC
     * if f + 1 <= #replies < 2f + 1
     */
    public boolean shouldPanic(long tolerance) {
        String currRequest = String.format("%s/%d", this.id, this.requestSequenceNumber.get());
        for (ClientReplyKey key : replies.get(currRequest).keySet()) {
            return this.replies.get(currRequest).get(key).size() >= tolerance + 1 
                && this.replies.get(currRequest).get(key).size() < tolerance * 2 + 1;
        }
        return false;
    }

    /**
     * Checks whether it has received 2f + 1 replies
     */
    public boolean completedReplies(long tolerance) {
        String currRequest = String.format("%s/%d", this.id, this.requestSequenceNumber.get());
        if (!replies.containsKey(currRequest)) {
            return false;
        }
        for (ClientReplyKey key : replies.get(currRequest).keySet()) {
            if (this.replies.get(currRequest).get(key).size() >= 2 * tolerance + 1) {
                return true;
            }
        }
        return false;
    }

    /**
     * Clear all timeouts for this client.
     */
    // public void clearAllTimeouts() {
    //     this.scenario.getTransport().clearClientTimeouts(this.id);
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
