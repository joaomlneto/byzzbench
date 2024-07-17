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

    private final Map<String, AdobCache> replicaLastCache = new HashMap<>();

    private AdobCache getReplicaLastCache(Replica r) {
        return replicaLastCache.getOrDefault(r.getNodeId(), root);
    }

    private VectorClock getReplicaClock(Replica r) {
        return clocks.computeIfAbsent(r.getNodeId(), k -> new VectorClock());
    }

    public synchronized void onLeaderChange(Replica r, String newLeaderId) {
        System.out.printf("%s: leader changed to %s%n", r.getNodeId(), newLeaderId);

        ElectionCache cache = null;

        // search for an ECache with the same leader ID
        for (AdobCache c : getReplicaLastCache(r).getChildren()) {
            if (c instanceof ElectionCache e && e.getLeader().equals(newLeaderId)) {
                cache = e;
                break;
            }
        }

        // create ECache if it doesn't exist
        if (cache == null) {
            long id = maxExistingCacheId.incrementAndGet();
            cache = new ElectionCache(id, getReplicaLastCache(r), r.getNodeId(), newLeaderId);
            caches.put(id, cache);
        }

        cache.addVoter(r.getNodeId());
        replicaLastCache.put(r.getNodeId(), cache);
    }

    // TODO: Whenever the leader does a local commit, create a new MCache
    public synchronized void onLocalCommit(Replica r, Serializable operation) {
        System.out.printf("%s: local commit%n", r.getNodeId());

        MethodCache cache = null;
        // search for an ECache with the same leader ID
        for (AdobCache c : getReplicaLastCache(r).getChildren()) {
            if (c instanceof MethodCache m) {
                Serializable a = m.getMethod();
                System.out.printf("Comparing %s to %s - %s%n", a, operation, a.equals(operation));
            }
            if (c instanceof MethodCache m && m.getMethod().toString().equals(operation.toString())) {
                cache = m;
                break;
            }
        }

        // create MCache
        if (cache == null) {
            long id = maxExistingCacheId.incrementAndGet();
            cache = new MethodCache(id, getReplicaLastCache(r), operation, r.getNodeId());
            caches.put(id, cache);
        }

        cache.addVoter(r.getNodeId());
        replicaLastCache.put(r.getNodeId(), cache);
    }

    // TODO: Whenever a replica times out and triggers an election, create a TCache
    public synchronized void onTimeout(Replica r) {
        System.out.printf("%s: timeout%n", r.getNodeId());
        // create TCache
        long id = maxExistingCacheId.incrementAndGet();
        TimeoutCache tCache = new TimeoutCache(id, root, Set.of(r.getNodeId()), Set.of(r.getNodeId()));

        caches.put(id, tCache);
    }

    // TODO: Whenever the leader forms a quorum, create a new CCache
    public synchronized void onQuorum(Replica r) {
        System.out.printf("%s: quorum%n", r.getNodeId());
        // create CCache
        long id = maxExistingCacheId.incrementAndGet();
        CommitCache cCache = new CommitCache(id, root, r.getNodeId());

        caches.put(id, cCache);
    }
}
