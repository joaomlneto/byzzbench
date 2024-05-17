package bftbench.runner.state;

import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A blockchain: a directed acyclic graph of blocks.
 * Each block has a parent block, except for the genesis block.
 *
 * @param <K> The key type of the blocks (e.g. hash)
 * @param <B> The type of the blocks
 */
@RequiredArgsConstructor
public class BlockDirectedAcyclicGraph<K, B extends PartialOrderLogEntry<K>> {
    private final Map<K, B> knownBlocks = new HashMap<>();
    private final Set<K> committedBlocks = new HashSet<>();
    private final TotalOrderCommitLog<B> log;

    public void add(K key, B block) {
        this.knownBlocks.put(key, block);
    }

    public B getBlock(K key) {
        return knownBlocks.get(key);
    }

    public void commitBlock(K key) {
        // Check if the block is known
        if (!knownBlocks.containsKey(key)) {
            throw new IllegalArgumentException("Cannot commit block: Block not found:");
        }
        B block = knownBlocks.get(key);

        // Check if the parent is known
        if (block.getParentHash() != null && knownBlocks.get(block.getParentHash()) == null) {
            throw new IllegalArgumentException("Cannot commit block: parent not found");
        }

        if (block.getParentHash() != null && !committedBlocks.contains(block.getParentHash())) {
            throw new IllegalArgumentException("Cannot commit block: parent not committed");
        }

        committedBlocks.add(key);
        this.log.add(block);
    }
}
