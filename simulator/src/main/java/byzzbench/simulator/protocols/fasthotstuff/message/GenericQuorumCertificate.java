package byzzbench.simulator.protocols.fasthotstuff.message;

import java.io.Serializable;
import java.util.Collection;

public interface GenericQuorumCertificate extends Serializable {
  Collection<? extends GenericVoteMessage> getVotes();
}
