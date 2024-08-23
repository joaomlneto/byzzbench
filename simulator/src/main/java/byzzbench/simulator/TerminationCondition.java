package byzzbench.simulator;


import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type", visible = true)
public abstract class TerminationCondition implements Serializable {
    public abstract boolean shouldTerminate();
}
