package bftbench.runner.pbft.message;

import lombok.Data;

import java.io.Serializable;
import java.util.Collection;

@Data
public class NewViewMessage<O> implements Serializable {
    private final long newViewNumber;
    private final Collection<ViewChangeMessage> viewChangeProofs;
    private final Collection<PrePrepareMessage<O>> preparedProofs;
}
