package byzzbench.simulator.protocols.pbft_java.message;

import byzzbench.simulator.transport.MessagePayload;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

@Data
@EqualsAndHashCode(callSuper = true)
@With
public class RequestMessage extends MessagePayload {
  private final Serializable operation;
  private final long timestamp;
  private final String clientId;

  @Override
  public String getType() {
    return "REQUEST";
  }
}
