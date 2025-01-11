package byzzbench.simulator.Zyzzyva;

import byzzbench.simulator.protocols.Zyzzyva.SpeculativeHistory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SpeculativeHistoryTests {

    @Test
    public void test() {}

    @Test
    public void historyTest() {
        SpeculativeHistory sh = new SpeculativeHistory();
        sh.add(0, 50L);
        assertEquals(50L, sh.get(0));
        assertEquals(50L, sh.getLast());
        sh.add(1, 40L);
        assertEquals(40L, sh.get(1));
        assertEquals(40L, sh.getLast());
        sh.truncate();
        assertEquals(40L, sh.get(1));
        assertEquals(1, sh.getSize());
    }
}
