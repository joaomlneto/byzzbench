package byzzbench.simulator.protocols.Zyzzyva.message;

import byzzbench.simulator.transport.MessagePayload;
import byzzbench.simulator.transport.messages.MessageWithRound;
import lombok.Data;
import lombok.With;

import java.io.Serializable;
import java.util.Objects;

@Data
@With
public class SpeculativeResponseWrapper extends MessagePayload implements Comparable<SpeculativeResponseWrapper>
        , MessageWithRound {
//{
    private final SpeculativeResponse specResponse;
    private final String replicaId;
    private final Serializable reply;
    private final OrderedRequestMessage orderedRequest;

    @Override
    public long getRound() {
        return (orderedRequest.getSequenceNumber() - 1) * 10 + 2;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        SpeculativeResponseWrapper that = (SpeculativeResponseWrapper) o;
        return Objects.equals(specResponse, that.specResponse) &&
//                Objects.equals(replicaId, that.replicaId) &&
                Objects.equals(reply, that.reply) &&
                Objects.equals(orderedRequest, that.orderedRequest);
    }

    @Override
    public int hashCode() {
        return Objects.hash(specResponse,
//                replicaId,
                reply,
                orderedRequest);
    }

    @Override
    public int compareTo(SpeculativeResponseWrapper o) {
        return this.getReplicaId().compareTo(o.getReplicaId());
    }

    @Override
    public String getType() {
        return "SPECULATIVE_RESPONSE_WRAPPER";
    }
}
