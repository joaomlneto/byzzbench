package byzzbench.simulator.adob;

/**
 * AdoB cache representing the root of the cache tree.
 * Not a real cache, but a placeholder for the root of the cache tree.
 */
public class RootCache extends AdobCache {
    protected RootCache() {
        super(null);
    }

    @Override
    public String getCacheType() {
        return "Root";
    }
}
