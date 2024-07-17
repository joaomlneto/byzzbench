package byzzbench.simulator.protocols.XRPL;

import java.util.Map;

public class DisputedTx {
    private String tx;
    private boolean ourVote;
    private int yesVotes;
    private int noVotes;
    Map<String, Boolean> votes;
}
