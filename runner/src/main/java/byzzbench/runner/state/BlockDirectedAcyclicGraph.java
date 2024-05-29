package byzzbench.runner.state;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

import java.io.Serializable;
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
@Log
public class BlockDirectedAcyclicGraph<K, B extends PartialOrderLogEntry<K>> implements Serializable {
    private final Map<K, B> knownBlocks = new HashMap<>();
    private final Set<K> committedBlocks = new HashSet<>();

    public void add(K key, B block) {
        this.knownBlocks.put(key, block);
    }

    public B getBlock(K key) {
        return knownBlocks.get(key);
    }

    public void commitBlock(K key) {
        // Check if the block is already committed
        if (committedBlocks.contains(key)) {
            throw new IllegalArgumentException("Cannot commit block: Block already committed");
        }

        // Check if the block is known
        if (!knownBlocks.containsKey(key)) {
            throw new IllegalArgumentException("Cannot commit block: Block not found:");
        }

        B block = knownBlocks.get(key);

        // Check if the parent is known
        if (block.getParentHash() != null && knownBlocks.get(block.getParentHash()) == null) {
            throw new IllegalArgumentException("Cannot commit block: parent not found");
        }

        // Check if the parent is committed
        if (block.getParentHash() != null && !committedBlocks.contains(block.getParentHash())) {
            throw new IllegalArgumentException("Cannot commit block: parent not committed");
        }

        committedBlocks.add(key);
    }
}
