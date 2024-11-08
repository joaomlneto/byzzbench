package byzzbench.simulator.protocols.hbft;

import byzzbench.simulator.Replica;
import byzzbench.simulator.Timekeeper;
import byzzbench.simulator.state.CommitLog;
import byzzbench.simulator.state.TotalOrderCommitLog;
import byzzbench.simulator.transport.MessagePayload;
import byzzbench.simulator.transport.Transport;
import java.io.Serializable;
import java.util.SortedSet;
import lombok.ToString;
import lombok.extern.java.Log;

/**
 * A Replica in the hBFT protocol.
 * <p>
 * Based on the publication by S. Duan, S. Peisert and K. N. Levitt:
 * hBFT: Speculative Byzantine Fault Tolerance with Minimum Cost
 */
@Log
@ToString(callSuper = true)
public class HbftReplica extends Replica {
  /**
   * TODO: Replica attributes go here, such as the current sequence number, the
   * current view, etc. <p> Remember to use {@link CommitLog} to store the state
   * of the replica! To do this, use the {@link #commitOperation} method to
   * append an entry to the log.
   */
  private final int sequenceNumber = 0; // TODO

  /**
   * Create a new replica.
   *
   * @param nodeId     the unique ID of the replica
   * @param nodeIds    the set of known node IDs in the system (excludes client
   *     IDs)
   * @param transport  the transport layer
   * @param timekeeper the timekeeper
   */
  protected HbftReplica(String nodeId, SortedSet<String> nodeIds,
                        Transport transport, Timekeeper timekeeper) {
    super(nodeId, nodeIds, transport, timekeeper, new TotalOrderCommitLog());
  }

  @Override
  public void initialize() {
    // TODO
  }

  @Override
  public void handleClientRequest(String clientId, Serializable request)
      throws Exception {
    // TODO
  }

  @Override
  public void handleMessage(String sender, MessagePayload m) {
    // TODO
  }
}
