package byzzbench.simulator;

import byzzbench.simulator.state.CommitLog;
import byzzbench.simulator.state.LogEntry;
import byzzbench.simulator.transport.DefaultClientReplyPayload;
import byzzbench.simulator.transport.MessagePayload;
import byzzbench.simulator.transport.Transport;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.java.Log;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Superclass for all replicas in the system.
 * <p>
 * Each replica has a unique node ID, a set of known node IDs in the system, a
 * reference to the {@link Transport} layer, and a {@link CommitLog}.
 */
@Log
@Getter
@ToString
public abstract class Replica implements Serializable, Node {
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
     * The commit log for this replica.
     */
    @Getter
    private final transient CommitLog commitLog;

    /**
     * The unique ID of the replica.
     */
    private final transient String id;

    /**
     * The Scenario object that this replica belongs to.
     */
    @JsonIgnore
    private final transient Scenario scenario;

    /**
     * The observers of this replica.
     */
    @JsonIgnore
    private final transient List<ReplicaObserver> observers = new java.util.ArrayList<>();

    /**
     * Create a new replica.
     *
     * @param id        the unique ID of the replica
     * @param scenario  the Scenario object that this replica belongs to
     * @param commitLog the commit log for this replica
     */
    protected Replica(String id, Scenario scenario, CommitLog commitLog) {
        this.id = id;
        this.scenario = scenario;
        this.commitLog = commitLog;
    }

    /**
     * Send a message to another node in the system.
     *
     * @param message   the message to send
     * @param recipient the recipient of the message
     */
    public void sendMessage(MessagePayload message, String recipient) {
        message.sign(this.id);
        this.scenario.getTransport().sendMessage(this.id, message, recipient);

    }

    /**
     * Send message to multiple recipients
     *
     * @param message    the message to send
     * @param recipients the recipients of the message
     */
    public void multicastMessage(MessagePayload message, SortedSet<String> recipients) {
        message.sign(this.id);
        this.scenario.getTransport().multicast(this.id, recipients, message);
    }

    /**
     * Send message to all nodes in the system (except self)
     *
     * @param message the message to broadcast
     */
    public void broadcastMessage(MessagePayload message) {
        SortedSet<String> otherNodes = this.getNodeIds().stream()
                .filter(otherNodeId -> !otherNodeId.equals(this.id))
                .collect(java.util.stream.Collectors.toCollection(TreeSet::new));

        message.sign(this.id);
        this.scenario.getTransport().multicast(this.id, otherNodes, message);
    }

    /**
     * Send message to all nodes in the system (including self)
     *
     * @param message the message to broadcast
     */
    public void broadcastMessageIncludingSelf(MessagePayload message) {
        message.sign(this.id);
        this.scenario.getTransport().multicast(this.id, this.getNodeIds(), message);
    }

    /**
     * Return the set of replica IDs in the system visible to this node.
     *
     * @return the set of replica IDs in the system visible to this node
     */
    public SortedSet<String> getNodeIds() {
        return this.scenario.getReplicaIds(this);
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

    /**
     * Initialize the replica. This method is called when the replica is
     * created. Subclasses should override this method to perform any
     * initialization that is required.
     */
    public void initialize() {
        // do nothing by default
    }

    /**
     * Handle a request received from a client.
     *
     * @param clientId the ID of the client
     * @param request  the request payload
     * @param timestamp the time the request was created/sent
     * @throws Exception if an error occurs while handling the request
     */
    public abstract void handleClientRequest(String clientId, long timestamp, Serializable request) throws Exception;

    /**
     * Send a reply to a client.
     *
     * @param clientId the ID of the client
     * @param reply    the reply payload
     */
    public void sendReplyToClient(String clientId, Serializable reply) {
        this.scenario.getTransport().sendClientResponse(this.id, new DefaultClientReplyPayload(reply), clientId);
    }

    /**
     * Send a reply to a client.
     * @param clientId the ID of the client
     * @param reply the reply payload
     * @param tolerance the tolerance of the protocol (used for hbft)
     */
    public void sendReplyToClient(String clientId, MessagePayload reply, long tolerance, long seqNumber) {
        this.scenario.getTransport().sendClientResponse(this.id, reply, clientId, tolerance, seqNumber);
    }

    /**
     * Handle a message received by this replica.
     *
     * @param sender  the ID of the sender
     * @param message the message payload
     * @throws Exception if an error occurs while handling the message
     */
    public abstract void handleMessage(String sender, MessagePayload message)
            throws Exception;

    /**
     * Commit an operation to the commit log and notify observers.
     *
     * @param operation the operation to commit
     */
    public void commitOperation(long sequenceNumber, LogEntry operation) {
        this.commitLog.add(sequenceNumber, operation);
        this.notifyObserversLocalCommit(operation);
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
        Runnable wrapper = () -> {
            this.notifyObserversTimeout();
            r.run();
        };
        return this.scenario.getTransport().setTimeout(this, wrapper, timeout);
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
     * Set a timeout for this replica.
     *
     * @param r       the runnable to execute when the timeout occurs
     * @param timeout the timeout in milliseconds
     * @param description the type of timeout
     * @return the timeout ID
     */
    public long setTimeout(Runnable r, long timeout, String description) {
        Runnable wrapper = () -> {
            this.notifyObserversTimeout();
            r.run();
        };
        Duration duration = Duration.ofSeconds(timeout);
        return this.scenario.getTransport().setTimeout(this, wrapper, duration, description);
    }

    /**
     * Clear timeout based on description.
     */
    public void clearTimeout(String description) {
        this.scenario.getTransport().clearTimeout(this, description);
    }

    /**
     * Clear all timeouts for this replica.
     */
    public void clearAllTimeouts() {
        this.scenario.getTransport().clearReplicaTimeouts(this);
    }

    /**
     * Add an observer to this replica.
     *
     * @param observer the observer to add
     */
    public void addObserver(ReplicaObserver observer) {
        this.observers.add(observer);
    }

    /**
     * Notify all observers that the view has changed.
     *
     * @param newLeaderId the new leader ID
     */
    public void notifyObserversLeaderChange(String newLeaderId) {
        System.out.println("Notifying observers of leader change: " + newLeaderId);
        System.out.println("Observers: " + this.observers);
        this.observers.forEach(observer -> observer.onLeaderChange(this, newLeaderId));
    }

    /**
     * Notify all observers that a local commit has occurred.
     *
     * @param operation the operation that was committed
     */
    public void notifyObserversLocalCommit(Serializable operation) {
        this.observers.forEach(observer -> observer.onLocalCommit(this, operation));
    }

    /**
     * Notify all observers that a timeout has occurred.
     */
    public void notifyObserversTimeout() {
        this.observers.forEach(observer -> observer.onTimeout(this));
    }

    public Instant getCurrentTime() {
        return this.scenario.getTimekeeper().getTime(this);
    }

    /**
     * Compute the difference between two times.
     *
     * @param end   the end time
     * @param start the start time
     * @return the difference between the two times
     */
    public Duration diffTime(Instant end, Instant start) {
        return Duration.between(start, end);
    }

    /**
     * Compute the difference between the current time and a start time.
     *
     * @param start the start time
     * @return the difference between the current time and the start time
     */
    public Duration diffNow(Instant start) {
        return this.diffTime(this.getCurrentTime(), start);
    }

}
