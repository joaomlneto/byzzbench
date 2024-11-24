package byzzbench.simulator;

import byzzbench.simulator.protocols.hbft.message.PanicMessage;
import byzzbench.simulator.protocols.hbft.message.RequestMessage;
import byzzbench.simulator.transport.Transport;
import byzzbench.simulator.utils.NonNull;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;


/**
 * Represents a client in the system. Each client has a unique identifier.
 * The client is responsible for sending requests to the replicas in the system.
 */
@Getter
@Builder
@RequiredArgsConstructor
public class Client implements Serializable {

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
    private final String clientId;

    /**
     * The sequence number of the next request to be sent by the client.
     */
    private final AtomicLong requestSequenceNumber = new AtomicLong(0);

    /**
     * The transport layer for the client.
     */
    @JsonIgnore
    @NonNull
    private final transient Transport transport;

    /**
     * The maximum number of requests that can be sent by the client.
     */
    private final long maxRequests = 3;

    /**
     * The maximum number of requests that can be sent concurrently by the client.
     */
    private final long maxConcurrentRequests = 1;

    /**
     * The replies received by the client.
     */
    private final SortedMap<Long, SortedMap<Long, Collection<Serializable>>> replies = new TreeMap<>();

    /**
     * The request sequence number already completed.
     */
    private final Set<Long> completedRequests = new HashSet<>();

    /**
     * The sent requests.
     */
    private final SortedMap<Long, RequestMessage> sentRequests = new TreeMap<>();

    /**
     * Timeout for client
     */
    private final long timeout = 5000;


    /**
     * Initializes the client by sending the initial requests.
     */
    public void initializeClient() {
        // Send the initial requests
        for (int i = 0; i < this.maxConcurrentRequests; i++) {
            this.sendRequest();
        }
    }

    /**
     * As of hBFT 4.1, sends a request to all replica in the system.
     */
    public void sendRequest() {
        String requestId = String.format("%s/%d", this.clientId, this.requestSequenceNumber.incrementAndGet());
        long timestamp = System.currentTimeMillis();
        RequestMessage request = new RequestMessage(requestId, timestamp, this.clientId);
        this.sentRequests.put(this.requestSequenceNumber.get(), request);
        this.transport.multicastClientRequest(this.clientId, timestamp, requestId, this.transport.getNodeIds());
        this.setTimeout(this::retransmitOrPanic, this.timeout);
    }

    public void retransmitOrPanic() {
        long tolerance = (long) Math.floor((this.transport.getNodeIds().size() - 1) / 3);
        if (this.shouldRetransmit(tolerance)) {
            String requestId = String.format("%s/%d", this.clientId, this.requestSequenceNumber.get());
            long timestamp = System.currentTimeMillis();
            this.transport.multicastClientRequest(this.clientId, timestamp, requestId, this.transport.getNodeIds());
        } else if (this.shouldPanic(tolerance)) {
            RequestMessage message = this.sentRequests.get(this.requestSequenceNumber.get());
            PanicMessage panic = new PanicMessage(this.digest(message), System.currentTimeMillis(), this.clientId);
            this.transport.multicast(this.clientId, this.transport.getNodeIds(), panic);
            this.setTimeout(this::retransmitOrPanic, this.timeout);
        }
    }

    /**
     * Handles a reply received by the client.
     * @param senderId The ID of the sender of the reply.
     * @param reply The reply received by the client.
     * @param tolerance the tolerance of the protocol (used for hbft)
     */
    public void handleReply(String senderId, Serializable reply, long tolerance, long seqNumber) {
        this.replies.putIfAbsent(this.requestSequenceNumber.get(), new TreeMap<>());
        this.replies.get(this.requestSequenceNumber.get()).putIfAbsent(seqNumber, new ArrayList<>());
        this.replies.get(this.requestSequenceNumber.get()).get(seqNumber).add(reply);

        /**
         * If the client received 2f + 1 correct replies,
         * and the request has not been completed yet.
         */
        if (this.completedReplies(tolerance, seqNumber) 
            && !this.completedRequests.contains(seqNumber) 
            && this.requestSequenceNumber.get() <= this.maxRequests) {
            this.completedRequests.add(seqNumber);
            this.sendRequest();
            this.clearAllTimeouts();
        }
    }

     /**
     * Handles a reply received by the client.
     * @param senderId The ID of the sender of the reply.
     * @param reply The reply received by the client.
     */
    public void handleReply(String senderId, Serializable reply) {
        // this.replies.putIfAbsent(this.requestSequenceNumber.get(), new ArrayList<>());
        // this.replies.get(this.requestSequenceNumber.get()).add(reply);
        if (this.requestSequenceNumber.get() < this.maxRequests) {
            this.sendRequest();
        }
    }

    /**
     * Checks whether client should retransmit the request
     * if #replies < f + 1
     */
    public boolean shouldRetransmit(long tolerance) {
        for (Long key : replies.get(this.requestSequenceNumber.get()).keySet()) {
            return !(this.replies.get(this.requestSequenceNumber.get()).get(key).size() >= tolerance + 1);
        }
        return true;
    }

    /**
     * Checks whether client should send PANIC
     * if f + 1 <= #replies < 2f + 1
     */
    public boolean shouldPanic(long tolerance) {
        for (Long key : replies.get(this.requestSequenceNumber.get()).keySet()) {
            return this.replies.get(this.requestSequenceNumber.get()).get(key).size() >= tolerance + 1 
                && this.replies.get(this.requestSequenceNumber.get()).get(key).size() < tolerance * 2 + 1;
        }
        return false;
    }

    /**
     * Checks whether it has received 2f + 1 replies
     */
    public boolean completedReplies(long tolerance, long seqNumber) {
        for (Long key : replies.get(this.requestSequenceNumber.get()).keySet()) {
            if (this.replies.get(this.requestSequenceNumber.get()).get(key).size() >= 2 * tolerance + 1) {
                return true;
            }
        }
        return false;
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
        return this.transport.setClientTimeout(this.clientId, wrapper, timeout);
    }

    /**
     * Clear all timeouts for this client.
     */
    public void clearAllTimeouts() {
        this.transport.clearClientTimeouts(this.clientId);
    }

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
