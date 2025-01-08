package byzzbench.simulator;

import byzzbench.simulator.transport.MessagePayload;
import byzzbench.simulator.utils.NonNull;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Null;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Represents a client in the system. Each client has a unique identifier.
 * The client is responsible for sending requests to the replicas in the system.
 */
@Getter
@Log
public class Client implements Serializable, Node {
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
    private final long maxRequests = 3;

    /**
     * The replies received by the client.
     */
    private final List<Serializable> replies = new ArrayList<>();

    /**
     * The iterator for cycling through replicas.
     */
    @JsonIgnore
    private Iterator<String> requestIterator;

    @Builder
    public Client(Scenario scenario, String id) {
        this.scenario = scenario;
        this.id = id;
        this.requestIterator = this.scenario.getReplicas().keySet().iterator();
    }

    @Override
    public void initialize() {
        // Send the first request
        this.sendRequest();
    }

    /**
     * Sends a request to a replica in the system.
     */
    public void sendRequest() {
        if (!this.requestIterator.hasNext()) {
            this.requestIterator = this.scenario.getReplicas().keySet().iterator();
        }
        String recipientId = requestIterator.next();
        log.info("Client " + this.id + " sending request to replica " + recipientId);
        String requestId = String.format("%s/%d", this.id, this.requestSequenceNumber.getAndIncrement());
        this.getScenario().getTransport().sendClientRequest(this.id, requestId, recipientId);
    }

    /**
     * Handles a reply received by the client.
     *
     * @param senderId The ID of the sender of the reply.
     * @param reply    The reply received by the client.
     */
    public void handleMessage(String senderId, MessagePayload reply) {
        log.info("Client received this message type" + reply.getType());
        switch (reply.getType()) {
            case "DefaultClientRequest":
                this.replies.add(reply);
                if (this.requestSequenceNumber.get() < this.maxRequests) {
                    this.sendRequest();
                }
                break;
            default:
                break;
        }
    }

    @Override
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
        return this.scenario.getTransport().setTimeout(this, r, timeout);
    }

    /**
     * Clear a timeout for this replica.
     *
     * @param eventId The event ID of the timeout to clear
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
