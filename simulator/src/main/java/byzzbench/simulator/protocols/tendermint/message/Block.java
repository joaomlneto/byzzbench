package byzzbench.simulator.protocols.tendermint.message;

import byzzbench.simulator.protocols.fasthotstuff.message.GenericQuorumCertificate;
import byzzbench.simulator.state.LogEntry;
import byzzbench.simulator.state.PartialOrderLogEntry;
import byzzbench.simulator.transport.MessagePayload;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.*;
import byzzbench.simulator.state.PartialOrderLogEntry;

@Data
@EqualsAndHashCode(callSuper = true)
public class Block extends MessagePayload implements LogEntry {
    private final long height;
    private final SortedSet<String> transactions;
    private final String previousBlockHash;

    public String getType() {
        return "BLOCK";
    }

}