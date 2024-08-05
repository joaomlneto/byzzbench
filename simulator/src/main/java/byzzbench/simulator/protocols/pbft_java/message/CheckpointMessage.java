package byzzbench.simulator.protocols.pbft_java.message;

import byzzbench.simulator.transport.MessagePayload;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

@Data
@EqualsAndHashCode(callSuper = true)
@With
public class CheckpointMessage extends MessagePayload {
  private final long lastSeqNumber;
  private final byte[] digest;
  private final String replicaId;

  @Override
  public String getType() {
    return "CHECKPOINT";
  }
}
