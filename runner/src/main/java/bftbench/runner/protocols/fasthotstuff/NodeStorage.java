package bftbench.runner.protocols.fasthotstuff;

import bftbench.runner.protocols.fasthotstuff.message.*;
import bftbench.runner.state.BlockDirectedAcyclicGraph;
import bftbench.runner.state.TotalOrderCommitLog;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.*;

public class NodeStorage implements Serializable {
    private final transient FastHotStuffReplica replica;
    //private final Map<ByteBuffer, Block> blocks = new HashMap<>();
    //private final Set<ByteBuffer> committedBlocks = new HashSet<>();
    private final Map<ByteBuffer, Set<VoteMessage>> votes = new HashMap<>();
    private final Map<Long, Set<NewViewMessage>> newViews = new HashMap<>();

    BlockDirectedAcyclicGraph<ByteBuffer, Block> dag;

    public NodeStorage(FastHotStuffReplica replica) {
        this.replica = replica;
        TotalOrderCommitLog<Block> log = new TotalOrderCommitLog<>();
        this.dag = new BlockDirectedAcyclicGraph<>(log);
    }

    // Adds a block to the storage
    public void addBlock(Block block) {
        //this.blocks.put(this.replica.hash(block), block);
        this.dag.add(this.replica.hash(block), block);
    }

    // Returns the block with the given hash
    public Block getBlock(ByteBuffer hash) {
        return this.dag.getBlock(hash);
        //return this.blocks.get(hash);
    }

    public Block getParentBlock(Block block) {
        return this.getBlock(block.getParentHash());
    }

    public void commit(Block block) {
        // TODO: Should we also commit all ancestors?
        //       Or maybe assert they are all committed?
        //this.committedBlocks.add(this.replica.hash(block));
        this.dag.commitBlock(this.replica.hash(block));
    }

    public Optional<QuorumCertificate> addVote(VoteMessage vote) {
        byte[] digest = vote.getBlockHash();
        Optional<Set<VoteMessage>> votes = canMakeQc(this.votes, ByteBuffer.wrap(digest), vote);
        if (votes.isPresent()) {
            return Optional.of(new QuorumCertificate(votes.get()));
        } else {
            return Optional.empty();
        }
    }

    public Optional<AggregateQuorumCertificate> addNewView(NewViewMessage newView) {
        long round = newView.getRound();
        Optional<Set<NewViewMessage>> newViews = canMakeQc(this.newViews, round, newView);
        if (newViews.isPresent()) {
            return Optional.of(new AggregateQuorumCertificate(newViews.get()));
        } else {
            return Optional.empty();
        }
    }

    public <K, V extends GenericVoteMessage> Optional<Set<V>> canMakeQc(Map<K, Set<V>> collection, K key, V value) {
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
