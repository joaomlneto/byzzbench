package byzzbench.simulator.Zyzzyva;

import byzzbench.simulator.protocols.Zyzzyva.message.OrderedRequestMessage;
import byzzbench.simulator.protocols.Zyzzyva.message.SpeculativeResponse;
import byzzbench.simulator.protocols.Zyzzyva.message.SpeculativeResponseWrapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SpeculativeResponseWrapperTests {

    @Test
    public void hashCodeAndEqualsTest() {
        SpeculativeResponse sr1 = new SpeculativeResponse(1, 2, 3, new byte[]{1, 2, 3}, "client1", 4);
        SpeculativeResponse sr2 = new SpeculativeResponse(1, 2, 3, new byte[]{1, 2, 3}, "client1", 4);

        OrderedRequestMessage orm1 = new OrderedRequestMessage(1, 2, 3, new byte[]{1, 2, 3});

        assertEquals(sr1, sr2);
        assertEquals(sr1.hashCode(), sr2.hashCode());

        SpeculativeResponseWrapper srw1 = new SpeculativeResponseWrapper(sr1, "replica1", "reply1", orm1);
        SpeculativeResponseWrapper srw2 = new SpeculativeResponseWrapper(sr2, "replica1", "reply1", orm1);

        assertEquals(srw1, srw2);
        assertEquals(srw1.hashCode(), srw2.hashCode());
    }
}
