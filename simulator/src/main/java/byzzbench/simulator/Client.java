package byzzbench.simulator;

import byzzbench.simulator.transport.Transport;
import byzzbench.simulator.utils.NonNull;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

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
  @NonNull private final String clientId;

  /**
   * The sequence number of the next request to be sent by the client.
   */
  private final AtomicLong requestSequenceNumber = new AtomicLong(0);

  /**
   * The transport layer for the client.
   */
  @JsonIgnore @NonNull private final transient Transport transport;

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
  private final List<Serializable> replies = new ArrayList<>();

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
   * Sends a request to a replica in the system.
   */
  public void sendRequest() {
    String recipientId =
        transport.getScenario().getNodes().keySet().iterator().next();
    String requestId = String.format(
        "%s/%d", this.clientId, this.requestSequenceNumber.getAndIncrement());
    this.transport.sendClientRequest(this.clientId, requestId, recipientId);
  }

  /**
   * Handles a reply received by the client.
   * @param senderId The ID of the sender of the reply.
   * @param reply The reply received by the client.
   */
  public void handleReply(String senderId, Serializable reply) {
    this.replies.add(reply);
    if (this.requestSequenceNumber.get() < this.maxRequests) {
      this.sendRequest();
    }
  }
}
