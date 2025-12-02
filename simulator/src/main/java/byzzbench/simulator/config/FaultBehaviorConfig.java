package byzzbench.simulator.config;


import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * Configuration for a behavior.
 */
@Data
@Builder
public class FaultBehaviorConfig implements Serializable {
    private final String faultBehaviorId;
    private final Map<String, String> params;
}
