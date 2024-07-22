package byzzbench.simulator;

import byzzbench.simulator.state.CommitLog;
import byzzbench.simulator.transport.MessagePayload;
import byzzbench.simulator.transport.Transport;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.java.Log;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Set;

/**
 * Superclass for all replicas in the system.
 * <p>
 * Each replica has a unique node ID, a set of known node IDs in the system, a
 * reference to the {@link Transport} layer, and a {@link CommitLog}.
 *
 * @param <T> The type of the entries in the commit log of each {@link Replica}.
 */
@Log
@Getter
@ToString
public abstract class Replica<T extends Serializable> implements Serializable {
    @JsonIgnore
    static MessageDigest md;

    static {
        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @Getter
    private final transient CommitLog<T> commitLog;

    private final transient String nodeId;

    @JsonIgnore
    private final transient Set<String> nodeIds;

    @JsonIgnore
    private final transient Transport<T> transport;

    @JsonIgnore
    private final transient List<ReplicaObserver> observers = new java.util.ArrayList<>();

    protected Replica(String nodeId, Set<String> nodeIds, Transport<T> transport,
                      CommitLog<T> commitLog) {
        this.nodeId = nodeId;
        this.nodeIds = nodeIds;
        this.transport = transport;
        this.commitLog = commitLog;
    }

    protected void sendMessage(MessagePayload message, String recipient) {
        this.transport.sendMessage(this.nodeId, message, recipient);
    }

    protected void multicastMessage(MessagePayload message,
                                    Set<String> recipients) {
        this.transport.multicast(this.nodeId, recipients, message);
    }

    /**
     * Send message to all nodes in the system (except self)
     *
     * @param message the message to broadcast
     */
    protected void broadcastMessage(MessagePayload message) {
        Set<String> otherNodes = this.nodeIds.stream()
                .filter(otherNodeId -> !otherNodeId.equals(this.nodeId))
                .collect(java.util.stream.Collectors.toSet());
        this.transport.multicast(this.nodeId, otherNodes, message);
    }

    protected void broadcastMessageIncludingSelf(MessagePayload message) {
        this.transport.multicast(this.nodeId, this.nodeIds, message);
    }

    public byte[] digest(Serializable message) {
        return md.digest(message.toString().getBytes());
    }


    public abstract void initialize();

    public abstract void handleMessage(String sender, MessagePayload message)
            throws Exception;

    public void commitOperation(T message) {
        this.commitLog.add(message);
        this.notifyObserversLocalCommit(message);
    }

    public long setTimeout(Runnable r, long timeout) {
        return this.transport.setTimeout(this, r, timeout);
    }

    public void clearAllTimeouts() {
        this.transport.clearReplicaTimeouts(this);
    }

    // Observer methods, for AdoB oracle
    // TODO: implicitly call these methods when the replica changes state!!

    // add observer
    public void addObserver(ReplicaObserver observer) {
        this.observers.add(observer);
    }

    public void notifyObserversLeaderChange(String newLeaderId) {
        this.observers.forEach(observer -> observer.onLeaderChange(this, newLeaderId));
    }

    public void notifyObserversLocalCommit(Serializable operation) {
        this.observers.forEach(observer -> observer.onLocalCommit(this, operation));
    }

    public void notifyObserversTimeout() {
        this.observers.forEach(observer -> observer.onTimeout(this));
    }

}
