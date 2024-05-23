package bftbench.runner.protocols.fasthotstuff.message;

import lombok.Data;

import java.io.Serializable;
import java.util.Collection;

@Data
public class AggregateQuorumCertificate implements Serializable, GenericQuorumCertificate {
    private final Collection<NewViewMessage> votes;
}
