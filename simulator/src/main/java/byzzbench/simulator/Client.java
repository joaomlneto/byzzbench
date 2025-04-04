package byzzbench.simulator;

import byzzbench.simulator.protocols.hbft.message.ClientRequestMessage;
import byzzbench.simulator.transport.MessagePayload;
import byzzbench.simulator.transport.Transport;
import byzzbench.simulator.utils.NonNull;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.java.Log;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Represents a client in the system. Each client has a unique identifier.
 * The client is responsible for sending requests to the replicas in the system.
 */
@Getter
@SuperBuilder
@RequiredArgsConstructor
@Log
public class Client extends Node implements Serializable {
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
     * The maximum number of requests that can be sent by the client.
     */
    private final long maxRequests = 1000;

    /**
     * The replies received by the client.
     */
    private final List<Serializable> replies = new ArrayList<>();

    @Override
    public void initialize() {
        // Send the first request
        this.sendRequest();
        //System.out.println("CLIENT TIMEOUT SETUP");
        //this.setTimeout("sendRequest", this::sendRequest, Duration.ofSeconds(1));
    }

    @Override
    public Transport getTransport() {
        return this.scenario.getTransport();
    }

    /**
     * Sends a request to any replica in the system.
     */
    public void sendRequest() {
        // get the IDs of replicas in the system (sorted)
        List<String> recipientIds = new ArrayList<>(this.getScenario().getReplicas().keySet().stream().sorted().toList());
        // select a "random" replica to send the request to
        String recipientId = recipientIds.get(this.getScenario().getRandom().nextInt(recipientIds.size()));
        this.sendRequest(recipientId);
    }

    /**
     * Sends a request to a given replica in the system.
     */
    public void sendRequest(String recipientId) {
        long sequenceNumber = getRequestSequenceNumber().incrementAndGet();
        String requestId = String.format("%s/%d", getId(), sequenceNumber);
        MessagePayload payload = new ClientRequestMessage(this.getCurrentTime().toEpochMilli(), requestId);
        this.getScenario().getTransport().sendMessage(this, payload, recipientId);
    }

    /**
     * Handles a reply received by the client.
     *
     * @param senderId The ID of the sender of the reply.
     * @param reply    The reply received by the client.
     */
    public void handleMessage(String senderId, MessagePayload reply) {
        this.replies.add(reply);
        if (this.requestSequenceNumber.get() < this.maxRequests) {
            this.sendRequest();
        }
    }

    @Override
    @JsonIgnore
    public Instant getCurrentTime() {
        return this.scenario.getTimekeeper().incrementAndGetTime(this);
    }

    /**
     * Set a timeout for this replica.
     *
     * @param name    a name for the timeout
     * @param r       the runnable to execute when the timeout occurs
     * @param timeout the timeout duration
     * @return the timer object
     */
    public long setTimeout(String name, Runnable r, Duration timeout) {
        return this.scenario.getTransport().setTimeout(this, r, timeout, "REQUEST");
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
}
