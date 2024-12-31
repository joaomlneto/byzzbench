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
    private final int hashOfLastBlock;

    private final RequestMessage requestMessage;

    public String getType() {
        return "BLOCK";
    }

    @Override
    public int compareTo(Block other) {
        return Comparator.comparing(Block::getId)
                .thenComparing(Block::getValue)
//                .thenComparingLong(Block::getHeight)
//                .thenComparingLong(Block::getRound)
                .compare(this, other);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Block block = (Block) o;
        return height == block.height &&
                round == block.round &&
                id == block.id &&
                Objects.equals(value, block.value);
    }

    @Override
    public int hashCode() {
        if(this == null)
            return 0;
        return Objects.hash(height, round, id, value);
    }



}