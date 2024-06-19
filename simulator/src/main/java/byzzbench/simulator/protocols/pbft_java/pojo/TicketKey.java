package byzzbench.simulator.protocols.pbft_java.pojo;

import lombok.Data;

import java.io.Serializable;

@Data
public class TicketKey implements Serializable {
    private final long viewNumber;
    private final long seqNumber;
}
