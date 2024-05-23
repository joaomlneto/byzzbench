package bftbench.runner.protocols.fasthotstuff;

import bftbench.runner.protocols.fasthotstuff.message.*;
import bftbench.runner.state.BlockDirectedAcyclicGraph;
import bftbench.runner.state.TotalOrderCommitLog;

import java.io.Serializable;
import java.util.*;

public class NodeStorage implements Serializable {
    private final transient FastHotStuffReplica replica;
    private final Map<String, Set<VoteMessage>> votes = new HashMap<>();
    private final Map<Long, Set<NewViewMessage>> newViews = new HashMap<>();

    private final BlockDirectedAcyclicGraph<String, Block> dag;

    public NodeStorage(FastHotStuffReplica replica) {
        this.replica = replica;
        TotalOrderCommitLog<Block> log = new TotalOrderCommitLog<>();
        this.dag = new BlockDirectedAcyclicGraph<>(log);
    }

    // Adds a block to the storage
    public void addBlock(Block block) {
        // if not there already, add it
        this.dag.add(this.replica.hash(block), block);
    }

    // Returns the block with the given hash
    public Block getBlock(String hash) {
        return this.dag.getBlock(hash);
    }

    // Returns the parent block of the given block
    public Block getParentBlock(Block block) {
        return this.getBlock(block.getParentHash());
    }

    // Commits a block
    public void commit(Block block) {
        this.dag.commitBlock(this.replica.hash(block));
    }

    // Adds a vote to the storage
    public Optional<QuorumCertificate> addVote(VoteMessage vote) {
        String digest = vote.getBlockHash();
        Optional<Set<VoteMessage>> votes = canMakeQc(this.votes, digest, vote);
        if (votes.isPresent()) {
            return Optional.of(new QuorumCertificate(votes.get()));
        } else {
            return Optional.empty();
        }
    }

    public Optional<AggregateQuorumCertificate> addVote(NewViewMessage newView) {
        long round = newView.getRound();
        Optional<Set<NewViewMessage>> newViewsQc = canMakeQc(this.newViews, round, newView);
        if (newViewsQc.isPresent()) {
            return Optional.of(new AggregateQuorumCertificate(newViewsQc.get()));
        } else {
            return Optional.empty();
        }
    }

    public <K, V extends GenericVoteMessage> Optional<Set<V>> canMakeQc(Map<K, Set<V>> collection, K key, V value) {
        System.out.println("Quorum size: " + this.replica.computeQuorumSize());
        boolean before = collection.containsKey(key) && collection.get(key).size() >= this.replica.computeQuorumSize();
        collection.computeIfAbsent(key, k -> new HashSet<>()).add(value);
        boolean after = collection.containsKey(key) && collection.get(key).size() >= this.replica.computeQuorumSize();
        if (after && !before) {
            return Optional.of(collection.get(key));
        } else {
            return Optional.empty();
        }
    }
}
