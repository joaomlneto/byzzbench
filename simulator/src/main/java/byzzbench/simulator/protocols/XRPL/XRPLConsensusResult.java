package byzzbench.simulator.protocols.XRPL;

import java.util.Map;
import java.util.Set;

public class XRPLConsensusResult {
    private Set<Integer> txSet;
    XRPLProposal proposal;
    Map<Integer, DisputedTx> disputeds;
    long roundTime;
}
