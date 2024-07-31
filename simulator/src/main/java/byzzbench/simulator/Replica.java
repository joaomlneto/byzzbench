package byzzbench.simulator;

import byzzbench.simulator.state.CommitLog;
import byzzbench.simulator.transport.ClientReplyPayload;
import byzzbench.simulator.transport.MessagePayload;
import byzzbench.simulator.transport.SignedMessagePayload;
import byzzbench.simulator.transport.Transport;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.java.Log;

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
  /**
   * The message digest algorithm to use for hashing messages.
   */
  @JsonIgnore static MessageDigest md;

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
  @Getter private final transient CommitLog<T> commitLog;

  /**
   * The unique ID of the replica.
   */
  private final transient String nodeId;

  /**
   * The set of known node IDs in the system (excludes client IDs).
   */
  @JsonIgnore private final transient Set<String> nodeIds;

  /**
   * The transport layer for this replica.
   */
  @JsonIgnore private final transient Transport<T> transport;

  /**
   * The observers of this replica.
   */
  @JsonIgnore
  private final transient List<ReplicaObserver> observers =
      new java.util.ArrayList<>();

  /**
   * Create a new replica.
   *
   * @param nodeId    the unique ID of the replica
   * @param nodeIds   the set of known node IDs in the system (excludes client
   *     IDs)
   * @param transport the transport layer
   * @param commitLog the commit log
   */
  protected Replica(String nodeId, Set<String> nodeIds, Transport<T> transport,
                    CommitLog<T> commitLog) {
    this.nodeId = nodeId;
    this.nodeIds = nodeIds;
    this.transport = transport;
    this.commitLog = commitLog;
  }

  /**
   * Send a message to another node in the system.
   *
   * @param message   the message to send
   * @param recipient the recipient of the message
   */
  protected void sendMessage(MessagePayload message, String recipient) {
    if (message instanceof SignedMessagePayload signableMessage) {
      signableMessage.sign(this.nodeId);
      this.transport.sendMessage(this.nodeId, signableMessage, recipient);
    } else {
      this.transport.sendMessage(this.nodeId, message, recipient);
    }
  }

  /**
   * Send message to multiple recipients
   *
   * @param message    the message to send
   * @param recipients the recipients of the message
   */
  protected void multicastMessage(MessagePayload message,
                                  Set<String> recipients) {
    if (message instanceof SignedMessagePayload signableMessage) {
      signableMessage.sign(this.nodeId);
      this.transport.multicast(this.nodeId, recipients, signableMessage);
    } else {
      this.transport.multicast(this.nodeId, recipients, message);
    }
  }

  /**
   * Send message to all nodes in the system (except self)
   *
   * @param message the message to broadcast
   */
  protected void broadcastMessage(MessagePayload message) {
    Set<String> otherNodes =
        this.nodeIds.stream()
            .filter(otherNodeId -> !otherNodeId.equals(this.nodeId))
            .collect(java.util.stream.Collectors.toSet());

    if (message instanceof SignedMessagePayload signableMessage) {
      signableMessage.sign(this.nodeId);
      this.transport.multicast(this.nodeId, otherNodes, signableMessage);
    } else {
      this.transport.multicast(this.nodeId, otherNodes, message);
    }
  }

  /**
   * Send message to all nodes in the system (including self)
   *
   * @param message the message to broadcast
   */
  protected void broadcastMessageIncludingSelf(MessagePayload message) {
    if (message instanceof SignedMessagePayload signableMessage) {
      signableMessage.sign(this.nodeId);
      this.transport.multicast(this.nodeId, this.nodeIds, signableMessage);
    } else {
      this.transport.multicast(this.nodeId, this.nodeIds, message);
    }
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

  public abstract void initialize();

  /**
   * Handle a request received from a client.
   *
   * @param clientId the ID of the client
   * @param request  the request payload
   * @throws Exception if an error occurs while handling the request
   */
  public abstract void handleClientRequest(String clientId,
                                           Serializable request)
      throws Exception;

  public void sendReplyToClient(String clientId, Serializable reply) {
    ClientReplyPayload response = new ClientReplyPayload(clientId, reply);
    this.transport.sendClientResponse(this.nodeId, response, clientId);
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
  public void commitOperation(T operation) {
    this.commitLog.add(operation);
    this.notifyObserversLocalCommit(operation);
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
      this.notifyObserversTimeout();
      r.run();
    };
    return this.transport.setTimeout(this, wrapper, timeout);
  }

  /**
   * Clear all timeouts for this replica.
   */
  public void clearAllTimeouts() { this.transport.clearReplicaTimeouts(this); }

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
    this.observers.forEach(
        observer -> observer.onLeaderChange(this, newLeaderId));
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
}
