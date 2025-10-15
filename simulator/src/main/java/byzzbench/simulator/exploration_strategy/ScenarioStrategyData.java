package byzzbench.simulator.exploration_strategy;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class ScenarioStrategyData implements Serializable {
    private final int remainingDropMessages;
    private final int remainingMutateMessages;
    private final boolean initializedByStrategy;
}
