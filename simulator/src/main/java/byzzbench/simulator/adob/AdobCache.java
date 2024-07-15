package byzzbench.simulator.adob;

import lombok.Getter;

import java.io.Serializable;
import java.util.List;

public abstract class AdobCache implements Serializable {
    @Getter
    private transient final AdobCache parent;

    @Getter
    private List<AdobCache> children;

    @Getter
    private long timestamp;

    protected AdobCache(AdobCache parent) {
        this.parent = parent;
    }

    public void addChildren(AdobCache child) {
        this.children.add(child);
    }

}
