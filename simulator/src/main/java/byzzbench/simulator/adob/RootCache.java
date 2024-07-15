package byzzbench.simulator.adob;

public class RootCache extends AdobCache {

    protected RootCache() {
        super(null);
    }

    @Override
    public String getCacheType() {
        return "Root";
    }
}
