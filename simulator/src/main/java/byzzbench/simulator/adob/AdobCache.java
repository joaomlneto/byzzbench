package byzzbench.simulator.adob;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;

import java.io.Serializable;
import java.util.List;

@Getter
public abstract class AdobCache implements Serializable {
    /**
     * The parent cache.
     */
    @JsonIgnore
    private final transient AdobCache parent;

    /**
     * The children cache.
     */
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
    protected AdobCache(AdobCache parent) {
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

    /**
     * Get the unique identifier for this cache.
     *
     * @return The unique identifier.
     */
    public String getId() {
        return Integer.toHexString(System.identityHashCode(this));
    }

}
