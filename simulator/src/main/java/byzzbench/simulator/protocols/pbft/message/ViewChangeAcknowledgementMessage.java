package byzzbench.simulator.protocols.pbft.message;

import byzzbench.simulator.protocols.pbft.CertifiableMessage;
import byzzbench.simulator.transport.MessagePayload;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;
import lombok.extern.java.Log;

/**
 * A ViewChangeAcknowledgement message: see View_change_ack.h/cc.
 * <p>
 * This is a message for replica replicaId stating that replica vcReplicaId sent
 * out a view-change message for view v with digest vcd. The MAC is for the
 * primary of v.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Log
@With
public class ViewChangeAcknowledgementMessage
    extends MessagePayload implements CertifiableMessage {
  public static final String TYPE = "ViewChangeAck";
  /**
   * The view number (v)
   */
  private final long viewNumber;

  /**
   * The destination replica's identifier (id)
   */
  private final long vcReplicaId;

  /**
   * The sending replica's identifier (vcid)
   */
  private final long replicaId;

  /**
   * The digest of the view-change message (vcd)
   */
  private final byte[] digest;

  /**
   * The MAC for the primary of view v.
   */
  private final String mac;

  @Override
  public String getType() {
    return TYPE;
  }

  @Override
  public boolean match(CertifiableMessage other) {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public String id() {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public boolean verify() {
    log.severe("verify(): Not implemented");
    return true;
    // throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public boolean full() {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public boolean encode() {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public boolean decode() {
    throw new UnsupportedOperationException("Not implemented");
  }
}
