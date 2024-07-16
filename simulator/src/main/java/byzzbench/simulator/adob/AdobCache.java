package byzzbench.simulator.adob;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 * The superclass for AdoB caches
 *
 * @see <a href="https://flint.cs.yale.edu/flint/publications/adob-tr.pdf">AdoB</a>
 */
@Getter
public abstract class AdobCache implements Serializable {
    private final long id;

    /**
     * The parent cache.
     */
    @JsonIgnore
    private final transient AdobCache parent;

    /**
     * The children cache.
     */
    @JsonIgnore
    private final List<AdobCache> children = new java.util.ArrayList<>();

    /**
     * Cache creation timestamp.
     */
    private long timestamp;

    /**
     * Create a new cache with a parent.
     *
     * @param parent The parent cache.
     */
    protected AdobCache(long id, AdobCache parent) {
        this.id = id;
        this.parent = parent;
    }

    /**
     * Add a child to the cache.
     *
     * @param child
     */
    public void addChildren(AdobCache child) {
        this.children.add(child);
    }

    /**
     * Get the cache type as a string.
     *
     * @return The cache type.
     */
    public abstract String getCacheType();

    public Optional<Long> getParentId() {
        return Optional.ofNullable(parent).map(AdobCache::getId);
    }

    public abstract byte getCRank();

}
