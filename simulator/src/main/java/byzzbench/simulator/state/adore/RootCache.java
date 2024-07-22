package byzzbench.simulator.state.adore;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * AdoB cache representing the root of the cache tree.
 * Not a real cache, but a placeholder for the root of the cache tree.
 */
public class RootCache extends AdoreCache {
    protected RootCache(long id) {
        super(id, null);
    }

    @Override
    public String getCacheType() {
        return "Root";
    }

    @Override
    @JsonIgnore
    public byte getCRank() {
        //throw new UnsupportedOperationException("Root cache does not have a rank.");
        return -1;
    }
}
