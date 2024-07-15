package byzzbench.simulator;

import java.io.Serializable;

public interface ReplicaObserver {
    void onLeaderChange(Replica r, String newLeaderId);

    void onLocalCommit(Replica r, Serializable operation);

    void onTimeout(Replica r);

    void onQuorum(Replica r);
}
