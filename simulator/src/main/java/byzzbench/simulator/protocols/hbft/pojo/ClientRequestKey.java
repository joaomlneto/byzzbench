package byzzbench.simulator.protocols.hbft.pojo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.Comparator;

@Data
public class ClientRequestKey implements Serializable, Comparable<ClientRequestKey> {
    private final long seqNum;
    private final String clientId;

    @Override
    public int compareTo(@NotNull ClientRequestKey other) {
        return Comparator.comparing(ClientRequestKey::getClientId)
                .thenComparingLong(ClientRequestKey::getSeqNum)
                .compare(this, other);
    }
}
