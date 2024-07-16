package byzzbench.simulator.adob;

import byzzbench.simulator.Replica;
import byzzbench.simulator.ReplicaObserver;
import lombok.Getter;
import lombok.Synchronized;
import lombok.extern.java.Log;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * The distributed state of the BFT consensus protocol for the current
 * simulation.
 *
 * @see <a href="https://flint.cs.yale.edu/flint/publications/adob-tr.pdf">AdoB
 * paper</a>
 */
@Log
public class AdobDistributedState implements ReplicaObserver, Serializable {
    @Getter(onMethod_ = {@Synchronized})
    private final AdobCache root = new RootCache();

    @Getter(onMethod_ = {@Synchronized})
    private final List<AdobCache> caches = List.of(root);

    // TODO: Whenever a replica changes its leader, create or update the respective ECache
    public synchronized void onLeaderChange(Replica r, String newLeaderId) {
        System.out.printf("%s: leader changed to %s\n", r.getNodeId(), newLeaderId);
        // create ECache
        ElectionCache eCache = new ElectionCache(root, r.getNodeId(), newLeaderId);

        root.addChildren(eCache);
    }

    // TODO: Whenever the leader does a local commit, create a new MCache
    public synchronized void onLocalCommit(Replica r, Serializable operation) {
        System.out.printf("%s: local commit\n", r.getNodeId());
        // create MCache
        MethodCache methodCache = new MethodCache(root, operation, r.getNodeId());

        root.addChildren(methodCache);
    }

    // TODO: Whenever a replica times out and triggers an election, create a TCache
    public synchronized void onTimeout(Replica r) {
        System.out.printf("%s: timeout\n", r.getNodeId());
        // create TCache
        TimeoutCache tCache = new TimeoutCache(root, Set.of(r.getNodeId()), Set.of(r.getNodeId()));

        root.addChildren(tCache);
    }

    // TODO: Whenever the leader forms a quorum, create a new CCache
    public synchronized void onQuorum(Replica r) {
        System.out.printf("%s: quorum\n", r.getNodeId());
        // create CCache
        CommitCache cCache = new CommitCache(root, r.getNodeId());

        root.addChildren(cCache);
    }
}
