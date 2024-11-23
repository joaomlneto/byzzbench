package byzzbench.simulator;

import byzzbench.simulator.transport.Transport;
import byzzbench.simulator.utils.NonNull;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;

import javax.sound.sampled.Line;

/**
 * Represents a client in the system. Each client has a unique identifier.
 * The client is responsible for sending requests to the replicas in the system.
 */
@Getter
@Builder
@RequiredArgsConstructor
public class Client implements Serializable {
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
    private final SortedMap<Long, List<Serializable>> replies = new TreeMap<>();

    /**
     * The request sequence number already completed.
     */
    private final Set<Long> completedRequests = new HashSet<>();

    /**
     * Timeout for client
     */
    private final long timeout = 1000;

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
        String recipientId = transport.getScenario().getNodes().keySet().iterator().next();
        String requestId = String.format("%s/%d", this.clientId, this.requestSequenceNumber.getAndIncrement());
        //for (String recipientId : transport.getScenario().getNodes().keySet()) {
            this.transport.sendClientRequest(this.clientId, requestId, recipientId);
        //}
        this.setTimeout(this::sendRequest, timeout);
    }

    public void retransmitRequest(long seqNumber) {
        if (this.shouldRetransmit(1, seqNumber)) {

        }
    }

    /**
     * Handles a reply received by the client.
     * @param senderId The ID of the sender of the reply.
     * @param reply The reply received by the client.
     * @param tolerance the tolerance of the protocol (used for hbft)
     */
    public void handleReply(String senderId, Serializable reply, long tolerance, long seqNumber) {
        if (this.replies.get(seqNumber) != null) {
            this.replies.get(seqNumber).add(reply);
        } else {
            this.replies.put(seqNumber, new ArrayList<>());
            this.replies.get(seqNumber).add(reply);
        }
        /**
         * If the client received 2f + 1 correct replies,
         * and the request has not been completed yet.
         */
        if (this.completedReplies(tolerance, seqNumber) 
            && !this.completedRequests.contains(seqNumber) 
            && this.requestSequenceNumber.get() < this.maxRequests) {
            this.completedRequests.add(seqNumber);
            this.sendRequest();
        }
    }

     /**
     * Handles a reply received by the client.
     * @param senderId The ID of the sender of the reply.
     * @param reply The reply received by the client.
     */
    public void handleReply(String senderId, Serializable reply) {
        if (this.replies.get(this.requestSequenceNumber.get()) != null) {
            this.replies.get(this.requestSequenceNumber.get()).add(reply);
        } else {
            this.replies.put(this.requestSequenceNumber.get(), new ArrayList<>());
            this.replies.get(this.requestSequenceNumber.get()).add(reply);
        }
        if (this.requestSequenceNumber.get() < this.maxRequests) {
            this.sendRequest();
        }
    }

    /**
     * Checks whether client should retransmit the request
     * if #replies < f + 1
     */
    public boolean shouldRetransmit(long tolerance, long seqNumber) {
        return this.replies.get(seqNumber).size() < tolerance + 1;
    }

    /**
     * Checks whether it has received 2f + 1 replies
     */
    public boolean completedReplies(long tolerance, long seqNumber) {
        return this.replies.get(seqNumber).size() >= 2 * tolerance + 1;
    }

    /**
     * Set a timeout for this replica.
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
     * Clear all timeouts for this replica.
     */
    public void clearAllTimeouts() {
        this.transport.clearClientTimeouts(this.clientId);
    }

}
