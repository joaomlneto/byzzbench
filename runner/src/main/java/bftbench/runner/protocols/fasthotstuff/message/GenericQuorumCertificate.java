package bftbench.runner.protocols.fasthotstuff.message;

import java.util.Collection;

public interface GenericQuorumCertificate {
    Collection<? extends GenericVoteMessage> getVotes();
}
