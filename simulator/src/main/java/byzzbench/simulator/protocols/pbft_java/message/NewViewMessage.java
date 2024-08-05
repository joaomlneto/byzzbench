package byzzbench.simulator.protocols.pbft_java.message;

import byzzbench.simulator.transport.MessagePayload;
import java.util.Collection;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

@Data
@EqualsAndHashCode(callSuper = true)
@With
public class NewViewMessage extends MessagePayload {
  private final long newViewNumber;
  private final Collection<ViewChangeMessage> viewChangeProofs;
  private final Collection<PrePrepareMessage> preparedProofs;

  @Override
  public String getType() {
    return "NEW-VIEW";
  }
}
