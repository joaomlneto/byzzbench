package byzzbench.simulator.protocols.fasthotstuff.message;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

@Data
@EqualsAndHashCode(callSuper = true)
@With
public class NewViewMessage extends GenericVoteMessage {
  private final GenericQuorumCertificate qc;
  private final long round;
  private final String author;

  @Override
  public String getType() {
    return "NEW-VIEW";
  }

  public String getBlockHash() {
    return qc.getVotes().stream().toList().get(0).getBlockHash();
  }
}
