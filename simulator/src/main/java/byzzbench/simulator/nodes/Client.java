package byzzbench.simulator.nodes;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.transport.MessagePayload;
import byzzbench.simulator.transport.Transport;
import byzzbench.simulator.utils.NonNull;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.java.Log;

import java.io.Serializable;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Represents a client in the system. Each client has a unique identifier.
 * The client is responsible for sending requests to the replicas in the system.
 */
@Getter
@RequiredArgsConstructor
@Log
public abstract class Client extends Node implements Serializable {
    /**
     * The set of request IDs that have been completed by the client.
     */
    protected final Set<Serializable> completedRequests = new HashSet<>();

    /**
     * The set of request IDs that have been issued by the client
     */
    protected final Set<Serializable> issuedRequests = new HashSet<>();

    /**
     * The scenario object that this client belongs to.
     */
    @JsonIgnore
    @NonNull
    private final transient Scenario scenario;

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
     * The replies received by the client for each request ID.
     */
    private final Map<Serializable, List<Serializable>> replies = new HashMap<>();

    /**
     * The timeout duration for the client to wait for a reply.
     */
    @Setter
    private Duration timeout = Duration.ofSeconds(60);

    /**
     * The current request ID being processed by the client.
     */
    private String currentRequestId;

    @Override
    public void initialize() {
        // Send the first request
        this.sendRequest();
    }

    /**
     * Generates a unique request ID for the client.
     *
     * @return The unique request ID.
     */
    public String generateRequestId() {
        this.currentRequestId = String.format("%s/%d", getId(), getRequestSequenceNumber().incrementAndGet());
        return currentRequestId;
    }

    @Override
    public Transport getTransport() {
        return this.scenario.getTransport();
    }

    /**
     * Returns the ID of a random replica in the system.
     *
     * @return a replica ID
     */
    public String getRandomRecipientId() {
        // get the IDs of replicas in the system (sorted)
        List<String> recipientIds = new ArrayList<>(this.getScenario().getReplicas().keySet().stream().sorted().toList());
        // select a "random" replica to send the request to
        return recipientIds.get(this.getScenario().getRandom().nextInt(recipientIds.size()));
    }

    /**
     * Issue a new request, and send it to a random replica in the system.
     */
    public void sendRequest() {
        String requestId = generateRequestId();
        this.issuedRequests.add(requestId);
        this.sendRequest(requestId, getRandomRecipientId());
    }

    /**
     * Sends a request to a given replica in the system.
     */
    public void sendRequest(String requestId, String recipientId) {
        MessagePayload payload = new ClientRequestMessage(requestId, this.getCurrentTime().toEpochMilli(), requestId);
        this.getScenario().getTransport().sendMessage(this, payload, recipientId);
        this.setTimeout(String.format("Request %s", requestId), this::retransmitRequest, this.timeout);
    }

    /**
     * Retransmits the current request
     */
    public void retransmitRequest() {
        this.sendRequest(this.getCurrentRequestId(), getRandomRecipientId());
    }

    /**
     * Handles a message received by the client.
     *
     * @param senderId The ID of the sender of the reply.
     * @param message  The message received by the client.
     */
    public void handleMessage(String senderId, MessagePayload message) {
        // check if the message is a valid reply payload
        if (!(message instanceof ClientReply reply)) {
            throw new IllegalArgumentException("Invalid reply type");
        }

        // add the reply to the replies map for the request ID
        System.out.println("Client " + this.getId() + " received reply for request " + reply.getRequestId() + " from " + senderId + ": " + reply.getReply());
        this.replies.computeIfAbsent(reply.getRequestId(), k -> new ArrayList<>())
                .add(reply.getReply());

        // check if the request is not marked as completed, but
        // it is now completed based on the this latest reply
        if (!this.completedRequests.contains(reply.getRequestId())
                && this.isRequestCompleted(reply)) {
            this.markRequestAsCompleted(reply.getRequestId());
        }
    }

    /**
     * Checks whether a request can now be marked as completed
     *
     * @param message the latest message payload received by the client
     * @return true if the request is now completed, false otherwise
     */
    public abstract boolean isRequestCompleted(ClientReply message);

    /**
     * Marks a request as completed by the client.
     *
     * @param requestId the ID of the request to mark as completed
     */
    protected void markRequestAsCompleted(Serializable requestId) {
        this.completedRequests.add(requestId);
        log.info(String.format("Request %s completed by client %s", requestId, this.getId()));
    }

}
