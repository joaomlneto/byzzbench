package byzzbench.simulator.exploration_strategy.twins;

import byzzbench.simulator.exploration_strategy.ScenarioStrategyData;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class TwinsScenarioStrategyData extends ScenarioStrategyData {
    /**
     * The number of replicas to create twins for.
     */
    private final int numReplicas;

    /**
     * The number of twins to create for each replica.
     */
    private final int numTwinsPerReplica;

    /**
     * The number of rounds to generate partitions for.
     */
    private final int numRounds;
}
