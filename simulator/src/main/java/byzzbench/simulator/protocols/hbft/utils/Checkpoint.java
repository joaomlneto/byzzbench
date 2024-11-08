package byzzbench.simulator.protocols.hbft.utils;

import byzzbench.simulator.protocols.hbft.SpeculativeHistory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Checkpoint {
    private long sequenceNumber;
    private SpeculativeHistory history;
}
