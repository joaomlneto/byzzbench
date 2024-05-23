package bftbench.runner.protocols.fasthotstuff.message;

import lombok.Data;

import java.io.Serializable;
import java.util.Collection;

@Data
public class QuorumCertificate implements Serializable, GenericQuorumCertificate {
    private final Collection<VoteMessage> votes;
}
