package byzzbench.simulator.state.adob;

import byzzbench.simulator.Client;
import byzzbench.simulator.Replica;
import byzzbench.simulator.ReplicaObserver;
import byzzbench.simulator.ScenarioObserver;
import byzzbench.simulator.versioning.VectorClock;
import lombok.Getter;
import lombok.Synchronized;
import lombok.extern.java.Log;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * The distributed state of the BFT consensus protocol for the current
 * simulation.
 *
 * @see <a href="https://flint.cs.yale.edu/flint/publications/adob-tr.pdf">AdoB
 * paper</a>
 */
@Log
public class AdobDistributedState implements ScenarioObserver, ReplicaObserver, Serializable {
    /**
     * The maximum cache ID that has been created so far.
     */
    private final AtomicLong maxExistingCacheId = new AtomicLong();
    /**
     * The vector clocks of each replica.
     */
    private final SortedMap<String, VectorClock> clocks = new TreeMap<>();
    /**
     * The last cache that each replica has created/supported.
     */
    private final SortedMap<String, AdobCache> replicaLastCache = new TreeMap<>();
    /**
     * The root cache of the distributed state.
     */
    @Getter(onMethod_ = {@Synchronized})
    private AdobCache root = new RootCache(0);
    /**
     * The caches that have been created so far.
     */
    @Getter(onMethod_ = {@Synchronized})
    private final SortedMap<Long, AdobCache> caches = new TreeMap<>(Map.of(0L, root));

    private AdobCache getReplicaLastCache(Replica r) {
        return replicaLastCache.getOrDefault(r.getId(), root);
    }

    private VectorClock getReplicaClock(Replica r) {
        return clocks.computeIfAbsent(r.getId(), k -> new VectorClock());
    }

    /**
     * Whenever the leader changes, create (or support) an ECache.
     *
     * @param r           the ID of the replica that changed view
     * @param newLeaderId the ID of the new leader
     */
    @Override
    public synchronized void onLeaderChange(Replica r, String newLeaderId) {
        log.info(String.format("%s: leader changed to %s%n", r.getId(), newLeaderId));

        ElectionCache electionCache = null;

        // search for an ECache with the same leader ID
        for (AdobCache c : getReplicaLastCache(r).getChildren()) {
            if (c instanceof ElectionCache e && e.getLeader().equals(newLeaderId)) {
                electionCache = e;
                break;
            }
        }

        // create ECache if it doesn't exist
        if (electionCache == null) {
            long id = maxExistingCacheId.incrementAndGet();
            electionCache = new ElectionCache(id, getReplicaLastCache(r), r.getId(), newLeaderId);
            caches.put(id, electionCache);
        }

        electionCache.addVoter(r.getId());
        replicaLastCache.put(r.getId(), electionCache);
    }

    /**
     * Whenever a replica commits an operation, create (or support) an MCache.
     *
     * @param r         the replica that committed the operation
     * @param operation the operation that was committed
     */
    @Override
    public synchronized void onLocalCommit(Replica r, Serializable operation) {
        log.info(String.format("%s: local commit%n", r.getId()));

        MethodCache methodCache = null;
        // search for an ECache with the same leader ID
        for (AdobCache c : getReplicaLastCache(r).getChildren()) {
            if (c instanceof MethodCache m) {
                Serializable a = m.getMethod();
                System.out.printf("Comparing %s to %s - %s%n", a, operation, a.equals(operation));
            }
            if (c instanceof MethodCache m && m.getMethod().toString().equals(operation.toString())) {
                methodCache = m;
                break;
            }
        }

        // create MCache
        if (methodCache == null) {
            long id = maxExistingCacheId.incrementAndGet();
            methodCache = new MethodCache(id, getReplicaLastCache(r), operation, r.getId());
            caches.put(id, methodCache);
        }

        methodCache.addVoter(r.getId());
        replicaLastCache.put(r.getId(), methodCache);

        // check if a majority of the replicas have committed the operation
        if (methodCache.getVoters().size() > replicaLastCache.size() / 2) {
            createOrSupportCCache(methodCache);
        }
    }

    /**
     * Create or support a CCache for the given MCache.
     *
     * @param parent an MCache that has a majority of votes
     */
    private synchronized void createOrSupportCCache(MethodCache parent) {
        // assert that the parent has enough votes
        assert parent.getVoters().size() > replicaLastCache.size() / 2;

        // if the MCache already has a CCache as a child, add the missing voter to the CCache
        for (AdobCache c : parent.getChildren()) {
            if (c instanceof CommitCache cCache) {
                cCache.addVoters(parent.getVoters());
                return;
            }
        }

        log.info(String.format("%s: quorum%n", parent));

        // create CCache
        long id = maxExistingCacheId.incrementAndGet();
        CommitCache cCache = new CommitCache(id, parent);
        caches.put(id, cCache);

        // add voters to CCache and update replica's last cache
        parent.getVoters().forEach(voter -> {
            cCache.addVoter(voter);
            replicaLastCache.put(voter, cCache);
        });
    }

    // TODO: Whenever a replica times out and triggers an election, create a TCache
    @Override
    public synchronized void onTimeout(Replica r) {
        log.info(String.format("%s: timeout%n", r.getId()));
        // create TCache
        long id = maxExistingCacheId.incrementAndGet();
        TimeoutCache tCache = TimeoutCache
                .builder()
                .id(id)
                .parent(root)
                .voters(new TreeSet<>(Collections.singleton(r.getId())))
                .supporters(new TreeSet<>(Collections.singleton(r.getId()))).build();
        caches.put(id, tCache);
    }

    public synchronized void reset() {
        caches.clear();
        replicaLastCache.clear();
        maxExistingCacheId.set(0);
        clocks.clear();

        root = new RootCache(0);
        caches.put(0L, root);
    }

    @Override
    public void onReplicaAdded(Replica r) {
        r.addObserver(this);
        log.info("Replica added: " + r.getId());
        CommitCache cCache = new CommitCache(r.getScenario().getReplicas().navigableKeySet().stream().sorted().toList().indexOf(r.getId()), root);
        caches.put(cCache.getId(), cCache);
    }

    @Override
    public void onClientAdded(Client c) {
        // do nothing
    }
}
