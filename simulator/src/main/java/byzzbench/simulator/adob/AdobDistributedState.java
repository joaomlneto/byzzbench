package byzzbench.simulator.adob;

import byzzbench.simulator.Replica;
import byzzbench.simulator.ReplicaObserver;
import byzzbench.simulator.versioning.VectorClock;
import lombok.Getter;
import lombok.Synchronized;
import lombok.extern.java.Log;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

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
    private final AdobCache root = new RootCache(0);

    @Getter(onMethod_ = {@Synchronized})
    private final Map<Long, AdobCache> caches = new HashMap<>(Map.of(0L, root));
    private final AtomicLong maxExistingCacheId = new AtomicLong();

    private final Map<String, VectorClock> clocks = new HashMap<>();

    private VectorClock getReplicaClock(Replica r) {
        return clocks.computeIfAbsent(r.getNodeId(), k -> new VectorClock());
    }

    // TODO: Whenever a replica changes its leader, create or update the respective ECache
    public synchronized void onLeaderChange(Replica r, String newLeaderId) {
        System.out.printf("%s: leader changed to %s\n", r.getNodeId(), newLeaderId);
        // create ECache
        long id = maxExistingCacheId.incrementAndGet();
        ElectionCache eCache = new ElectionCache(id, root, r.getNodeId(), newLeaderId);

        caches.put(id, eCache);
        root.addChildren(eCache);
    }

    // TODO: Whenever the leader does a local commit, create a new MCache
    public synchronized void onLocalCommit(Replica r, Serializable operation) {
        System.out.printf("%s: local commit\n", r.getNodeId());
        // create MCache
        long id = maxExistingCacheId.incrementAndGet();
        MethodCache methodCache = new MethodCache(id, root, operation, r.getNodeId());

        caches.put(id, methodCache);
        root.addChildren(methodCache);
    }

    // TODO: Whenever a replica times out and triggers an election, create a TCache
    public synchronized void onTimeout(Replica r) {
        System.out.printf("%s: timeout\n", r.getNodeId());
        // create TCache
        long id = maxExistingCacheId.incrementAndGet();
        TimeoutCache tCache = new TimeoutCache(id, root, Set.of(r.getNodeId()), Set.of(r.getNodeId()));

        caches.put(id, tCache);
        root.addChildren(tCache);
    }

    // TODO: Whenever the leader forms a quorum, create a new CCache
    public synchronized void onQuorum(Replica r) {
        System.out.printf("%s: quorum\n", r.getNodeId());
        // create CCache
        long id = maxExistingCacheId.incrementAndGet();
        CommitCache cCache = new CommitCache(id, root, r.getNodeId());

        caches.put(id, cCache);
        root.addChildren(cCache);
    }
}
