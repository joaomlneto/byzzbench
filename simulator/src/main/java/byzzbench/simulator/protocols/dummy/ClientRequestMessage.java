package byzzbench.simulator.protocols.dummy;

import byzzbench.simulator.transport.MessagePayload;
import java.io.Serializable;
import lombok.Data;

@Data
public class ClientRequestMessage extends MessagePayload {
  private final Serializable payload;
  @Override
  public String getType() {
    return "ClientRequestMessage";
  }
}
