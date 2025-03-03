package byzzbench.simulator.protocols.hbft.pojo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.Comparator;

@Data
public class ClientReplyKey implements Serializable, Comparable<ClientReplyKey> {
    private final String response;
    private final long seqNumber;

    @Override
    public int compareTo(@NotNull ClientReplyKey other) {
        return Comparator.comparing(ClientReplyKey::getResponse)
                .thenComparingLong(ClientReplyKey::getSeqNumber)
                .compare(this, other);
    }
}
