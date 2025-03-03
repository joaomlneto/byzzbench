package byzzbench.simulator.protocols.fab2;

import lombok.Getter;

public class LeaderElection {
    private final int p;
    private final int f;
    @Getter
    private final int regency;
    private int suspectingNodes = 0;

    public LeaderElection(int p, int f, int regency) {
        this.p = p;
        this.f = f;
        this.regency = regency;
    }

    public String getLeaderId() {
        return Character.toString(((char) 'A' + getRegency() % p));
    }

    public boolean suspect(int regency) {
        int quorum = (int) Math.ceil((p + f + 1) / 2.0);
        if (regency == this.regency) {
            suspectingNodes++;
        }

        return suspectingNodes > quorum;
    }

    private void reset() {
        suspectingNodes = 0;
    }
}
