package byzzbench.simulator.protocols.hbft;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.nodes.Client;
import byzzbench.simulator.protocols.hbft.message.*;
import byzzbench.simulator.protocols.hbft.pojo.ClientReplyKey;
import byzzbench.simulator.transport.DefaultClientReplyPayload;
import byzzbench.simulator.transport.MessagePayload;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;


/**
 * Represents a client in the system. Each client has a unique identifier.
 * The client is responsible for sending requests to the replicas in the system.
 */
@Getter
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
    //private final Set<ClientReplyKey> completedRequests = new HashSet<>();

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

    public HbftClient(Scenario scenario, String id) {
        super(scenario, id);
    }

    /**
     * As of hBFT 4.1, sends a request to all replica in the system.
     */
    @Override
    public void sendRequest() {
        String requestId = String.format("%s/%d", getId(), getRequestSequenceNumber().incrementAndGet());
        long timestamp = this.getCurrentTime().toEpochMilli();
        RequestMessage request = new RequestMessage(requestId, timestamp, getId());
        this.sentRequests.put(getRequestSequenceNumber().get(), request);
        this.sentRequestsByTimestamp.put(timestamp, requestId);
        this.broadcastRequest(timestamp, requestId);

        // Set timeout
        Long timeoutId = this.setTimeout("REQUEST", this::retransmitOrPanic, this.getTimeout());
        timeouts.put(getRequestSequenceNumber().get(), timeoutId);
    }

    public void retransmitOrPanic() {
        long tolerance = (long) Math.floor((getScenario().getTransport().getNodeIds().size() - 1) / 3);
        if (this.shouldRetransmit(tolerance)) {
            String requestId = String.format("%s/%d", getId(), getRequestSequenceNumber().get());
            // Based on hBFT 4.1 it uses the identical request
            // TODO: It probably should not be the same timestamp
            long timestamp = this.sentRequests.get(getRequestSequenceNumber().get()).getTimestamp();
            this.broadcastRequest(timestamp, requestId);
        } else if (this.shouldPanic(tolerance)) {
            RequestMessage message = this.sentRequests.get(getRequestSequenceNumber().get());
            PanicMessage panic = new PanicMessage(this.digest(message), this.getCurrentTime().toEpochMilli(), getId());
            getScenario().getTransport().multicast(this, getScenario().getTransport().getNodeIds(), panic);
        }
        this.clearTimeout(timeouts.get(getRequestSequenceNumber().get()));
        Long timeoutId = this.setTimeout("REQUEST", this::retransmitOrPanic, this.getTimeout());
        timeouts.put(getRequestSequenceNumber().get(), timeoutId);
    }

    private void broadcastRequest(long timestamp, String requestId) {
        MessagePayload payload = new ClientRequestMessage(timestamp, requestId);
        SortedSet<String> replicaIds = getScenario().getTransport().getNodeIds();
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
                && !this.completedRequests.contains(key)) {
            this.completedRequests.add(key);
            this.clearTimeout(this.timeouts.get(getRequestSequenceNumber().get()));
            this.sendRequest();
        }
    }

    @Override
    public boolean isRequestCompleted(DefaultClientReplyPayload message) {
        // we use custom logic. this should not be called!
        throw new UnsupportedOperationException("isRequestCompleted is not supported in HbftClient");
    }

    /**
     * Checks whether client should retransmit the request
     * if #replies < f + 1
     */
    public boolean shouldRetransmit(long tolerance) {
        String currRequest = String.format("%s/%d", getId(), getRequestSequenceNumber().get());
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
        String currRequest = String.format("%s/%d", getId(), getRequestSequenceNumber().get());
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
        String currRequest = String.format("%s/%d", getId(), getRequestSequenceNumber().get());
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
     * Create a digest of a message.
     *
     * @param message the message to digest
     * @return the digest of the message
     */
    public byte[] digest(Serializable message) {
        return md.digest(message.toString().getBytes());
    }

}
