package byzzbench.simulator.protocols.Zyzzyva.message;

import byzzbench.simulator.transport.MessagePayload;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

import java.util.SortedMap;

@EqualsAndHashCode(callSuper = true)
@Data
@With
public class FillHoleMap extends MessagePayload {
    private final SortedMap<Long, FillHoleReply> fillHoleMap;

    @Override
    public String getType() {
        return "FILL_HOLE_MAP";
    }
}
