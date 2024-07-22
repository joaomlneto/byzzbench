package byzzbench.simulator.state.adore;

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
public abstract class AdoreCache implements Serializable {
    private final long id;

    /**
     * The parent cache.
     */
    @JsonIgnore
    private final transient AdoreCache parent;

    /**
     * The children cache.
     */
    @JsonIgnore
    private final List<AdoreCache> children = new java.util.ArrayList<>();

    /**
     * Cache creation timestamp.
     */
    private long timestamp;

    /**
     * Create a new cache with a parent.
     *
     * @param parent The parent cache.
     */
    protected AdoreCache(long id, AdoreCache parent) {
        this.id = id;
        this.parent = parent;
        if (parent != null) {
            parent.addChildren(this);
        }
    }

    /**
     * Add a child to the cache.
     *
     * @param child The child cache to append to the list of children.
     */
    public void addChildren(AdoreCache child) {
        this.children.add(child);
    }

    /**
     * Get the cache type as a string.
     *
     * @return The cache type.
     */
    public abstract String getCacheType();

    public Optional<Long> getParentId() {
        return Optional.ofNullable(parent).map(AdoreCache::getId);
    }

    @JsonIgnore
    public abstract byte getCRank();

}
