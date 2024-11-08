package byzzbench.simulator.protocols.pbft.message;

import byzzbench.simulator.protocols.pbft.PbftReplica;
import byzzbench.simulator.transport.MessagePayload;
import java.util.SortedMap;
import java.util.logging.Level;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;
import lombok.extern.java.Log;

/**
 * A "NewKey" message see New_key.h/cc.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@With
@Log
@AllArgsConstructor
public class NewKeyMessage extends MessagePayload {
  public static final String TYPE = "NewKey";
  /**
   * Unique request identifier (rid).
   */
  private final long rid;

  /**
   * ID of the replica that generated the message. (id)
   */
  private final String id;

  /**
   * Keys for all replicas except "id" in order of increasing identifiers.
   * Each key has size Nonce_size bytes and is encrypted with the public-key of
   * the corresponding replica.
   */
  private final SortedMap<String, String> keys = new java.util.TreeMap<>();

  /**
   * Signature from principal id.
   */
  private final String signature;

  public NewKeyMessage(PbftReplica replica) {
    int[] k = new int[replica.n()]; // FIXME: No idea!
    this.rid = replica.new_rid();
    replica.getPrincipal().set_out_key(replica, k, rid);
    this.id = replica.id();

    // Get new keys and encrypt them
    log.log(Level.SEVERE, "PKI not yet implemented!!");

    // Compute signature and update size
    // TODO: sign properly
    this.signature = replica.id();
  }

  @Override
  public String getType() {
    return TYPE;
  }

  public boolean verify() {
    log.severe("NewKeyMessage::verify(): Not implemented");
    return true;
    // throw new UnsupportedOperationException("Not implemented");
  }
}
