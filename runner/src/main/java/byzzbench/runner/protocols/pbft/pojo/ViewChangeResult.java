package byzzbench.runner.protocols.pbft.pojo;

import lombok.Data;

import java.io.Serializable;

@Data
public class ViewChangeResult implements Serializable {
    private final boolean shouldBandwagon;
    private final long bandwagonViewNumber;
    private final boolean beginNextVote;
}
