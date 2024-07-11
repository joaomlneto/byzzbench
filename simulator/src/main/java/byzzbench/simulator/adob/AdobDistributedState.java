package byzzbench.simulator.adob;

import byzzbench.simulator.Replica;

import java.io.Serializable;

/**
 * The distributed state of the BFT consensus protocol for the current simulation.
 *
 * @see <a href="https://flint.cs.yale.edu/flint/publications/adob-tr.pdf">AdoB paper</a>
 */
public class AdobDistributedState {
    private AdobCache root;

    public CommitCache getLatestCommitCache() {
        AdobCache cache = root;
        while (cache instanceof CommitCache) {
            cache = cache.getParent();
        }
        return (CommitCache) cache;
    }

    // TODO: Whenever a replica changes its leader, create or update the respective ECache
    public void onLeaderChange(Replica r, String newLeaderId) {
        throw new RuntimeException("Not yet implemented");
    }

    // TODO: Whenever the leader does a local commit, create a new MCache
    public void onLocalCommit(Replica r, Serializable operation) {
        throw new RuntimeException("Not yet implemented");
    }

    // TODO: Whenever a replica times out and triggers an election, create a TCache
    public void onTimeout(Replica r) {
        throw new RuntimeException("Not yet implemented");
    }

    // TODO: Whenever the leader forms a quorum, create a new CCache
    public void onQuorum(Replica r) {
        throw new RuntimeException("Not yet implemented");
    }

}
