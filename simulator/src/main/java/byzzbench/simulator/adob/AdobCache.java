package byzzbench.simulator.adob;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;

import java.io.Serializable;
import java.util.List;

@Getter
public abstract class AdobCache implements Serializable {
    @JsonIgnore
    private final transient AdobCache parent;

    private final List<AdobCache> children = new java.util.ArrayList<>();

    private long timestamp;

    protected AdobCache(AdobCache parent) {
        this.parent = parent;
    }

    public void addChildren(AdobCache child) {
        this.children.add(child);
    }

    public abstract String getCacheType();

    public String getId() {
        return Integer.toHexString(System.identityHashCode(this));
    }

}
