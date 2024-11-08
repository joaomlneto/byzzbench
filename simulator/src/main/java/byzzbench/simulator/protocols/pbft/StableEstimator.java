package byzzbench.simulator.protocols.pbft;

import byzzbench.simulator.protocols.pbft.message.ReplyStableMessage;

/**
 * Used to estimate the maximum stable checkpoint sequence number at any
 * non-faulty replica by collecting reply-stable messages.
 */
public class StableEstimator {
  /**
   * Adds message "m" to this and returns true if estimation is complete.
   * "mine" should be true iff the message was sent by the caller.
   *
   * @param m    the message to add
   * @param mine whether the message was sent by the caller
   * @return true if estimation is complete
   */
  public boolean add(ReplyStableMessage m, boolean mine) {
    throw new UnsupportedOperationException("Not implemented");
  }

  /**
   * Adds message "m" to this and returns true if estimation is complete.
   * "m" is assumed to not be sent by the caller.
   *
   * @param m the message to add
   * @return true if estimation is complete
   */
  public boolean add(ReplyStableMessage m) { return this.add(m, false); }

  /**
   * If the estimation is not complete, returns -1.
   * Otherwise, returns the estimate of the maximum stable checkpoint
   * sequence number at any non-faulty replica. This estimate is
   * a conservative upper bound.
   *
   * @return the estimate of the maximum stable checkpoint sequence number
   */
  public long estimate() {
    throw new UnsupportedOperationException("Not implemented");
  }

  /**
   * Returns the maximum sequence number for a checkpoint that is known
   * to be stable. This estimate is a lower bound.
   *
   * @return the maximum sequence number for a checkpoint that is known to be
   *     stable
   */
  public long low_estimate() {
    throw new UnsupportedOperationException("Not implemented");
  }

  /**
   * If the estimation is complete, it has no effect,
   * otherwise discards all the information in this
   */
  public void mark_stale() {
    throw new UnsupportedOperationException("Not implemented");
  }

  /**
   * Discards all messages in this
   */
  public void clear() {
    throw new UnsupportedOperationException("Not implemented");
  }
}
