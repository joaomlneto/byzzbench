package byzzbench.simulator.exploration_strategy;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@Getter
@SuperBuilder
public class ScenarioStrategyData implements Serializable {
    private final boolean initializedByStrategy;
    private final int remainingDropMessages;
    private final int remainingMutateMessages;
}
