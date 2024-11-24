package byzzbench.simulator.protocols.hbft.pojo;

import lombok.Data;

import java.io.Serializable;

@Data
public class ViewChangeResult implements Serializable {
    private final boolean shouldBandwagon;
    private final boolean shouldSendNewView;
}
