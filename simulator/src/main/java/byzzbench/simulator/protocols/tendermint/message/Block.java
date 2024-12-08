package byzzbench.simulator.protocols.tendermint.message;

import byzzbench.simulator.protocols.fasthotstuff.message.GenericQuorumCertificate;
import byzzbench.simulator.state.LogEntry;
import byzzbench.simulator.state.PartialOrderLogEntry;
import byzzbench.simulator.transport.MessagePayload;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.*;
import byzzbench.simulator.state.PartialOrderLogEntry;
import lombok.Getter;
import lombok.Setter;

@Data
public class Block implements LogEntry, Comparable<Block> {
    private final long height;
    private final long round;
    private final long id;
    private final String value;

    private final RequestMessage requestMessage;

    public String getType() {
        return "BLOCK";
    }

    @Override
    public int compareTo(Block other) {
        return Comparator.comparing(Block::getId)
                .thenComparing(Block::getValue)
                .compare(this, other);
    }
}