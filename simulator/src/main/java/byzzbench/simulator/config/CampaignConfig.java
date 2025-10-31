package byzzbench.simulator.config;

import byzzbench.simulator.domain.Campaign;
import byzzbench.simulator.domain.ScenarioParameters;
import byzzbench.simulator.exploration_strategy.ExplorationStrategyParameters;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Represents the configuration for setting up a {@link Campaign}
 */
@Data
@NoArgsConstructor
public class CampaignConfig implements Serializable {
    /**
     * Whether to run this campaign at startup or not
     */
    private boolean autoStart;

    /**
     * The initial random seed, from which to seed each scenario
     */
    private long initialRandomSeed;

    /**
     * The ID of the exploration strategy to be used
     */
    private String explorationStrategyId;

    /**
     * The parameters for the exploration strategy
     */
    private ExplorationStrategyParameters explorationStrategyParameters;

    /**
     * The number of scenarios to run for this campaign
     */
    private long numScenarios;

    /**
     * The parameters to initialize the scenario
     */
    private ScenarioParameters scenarioParameters;

    /**
     * Parameters for terminating the execution of a scenario
     */
    private TerminationConfig termination;
}
