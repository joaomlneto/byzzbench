package byzzbench.simulator.nodes;

import java.io.Serializable;

public interface ReplicaObserver {
    void onLeaderChange(Replica r, String newLeaderId);

    void onLocalCommit(Replica r, Serializable operation);

    void onTimeout(Replica r);
}
