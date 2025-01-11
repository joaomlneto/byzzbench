package byzzbench.simulator.Zyzzyva;

import byzzbench.simulator.protocols.Zyzzyva.message.FillHoleReply;
import byzzbench.simulator.protocols.Zyzzyva.message.OrderedRequestMessage;
import byzzbench.simulator.protocols.Zyzzyva.message.RequestMessage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FillHoleTests {

    @Test
    public void equalsReplyTest() {
        OrderedRequestMessage orm1 = new OrderedRequestMessage(1, 2, 3, new byte[]{4, 5, 6});
        OrderedRequestMessage orm2 = new OrderedRequestMessage(1, 2, 3, new byte[]{4, 5, 6});

        assertEquals(orm1, orm2);

        RequestMessage r1 = new RequestMessage("op", 1, "cid");
        RequestMessage r2 = new RequestMessage("op", 1, "cid");

        assertEquals(r1, r2);

        FillHoleReply fhr1 = new FillHoleReply(orm1, r1);
        FillHoleReply fhr2 = new FillHoleReply(orm2, r2);

        assertEquals(fhr1, fhr2);

    }

    @Test
    public void hashTest() {
        OrderedRequestMessage orm1 = new OrderedRequestMessage(1, 2, 3, new byte[]{4, 5, 6});
        OrderedRequestMessage orm2 = new OrderedRequestMessage(1, 2, 3, new byte[]{4, 5, 6});

        assertEquals(orm1.hashCode(), orm2.hashCode());

        RequestMessage r1 = new RequestMessage("op", 1, "cid");
        RequestMessage r2 = new RequestMessage("op", 1, "cid");

        assertEquals(r1.hashCode(), r2.hashCode());

        FillHoleReply fhr1 = new FillHoleReply(orm1, r1);
        FillHoleReply fhr2 = new FillHoleReply(orm2, r2);

        assertEquals(fhr1.hashCode(), fhr2.hashCode());

    }
}
