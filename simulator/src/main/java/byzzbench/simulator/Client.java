package byzzbench.simulator;

import byzzbench.simulator.transport.Transport;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.util.Random;
import java.util.Set;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Represents a client in the system. Each client has a unique identifier.
 * The client is responsible for sending requests to the replicas in the system.
 *
 * @param <T> The type of the entries in the commit log of each {@link Replica}.
 */
@Getter
@RequiredArgsConstructor
public class Client<T extends Serializable> implements Serializable {
  /**
   * The unique ID of the client.
   */
  private final String clientId;

  /**
   * The transport layer for the client.
   */
  @JsonIgnore private final transient Transport<T> transport;

  private final long maxRequests = 1;

  public void initialize() {}

  protected void sendRequestToRandomReplica() {
    // send a request message to a random replica
    Set<String> nodeIds = transport.getNodeIds();

    // randomly select a replica to send the request to
    long nodeIndex = new Random().nextInt(nodeIds.size());
  }

  public void handleReply(String senderId, Serializable reply) {
    // TODO
  }
}
