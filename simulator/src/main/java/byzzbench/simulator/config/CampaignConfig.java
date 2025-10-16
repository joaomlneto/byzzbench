package byzzbench.simulator.config;

import byzzbench.simulator.domain.ScenarioParameters;
import byzzbench.simulator.exploration_strategy.ExplorationStrategyParameters;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CampaignConfig {
    private long initialRandomSeed;
    private String explorationStrategyId;
    private long numScenarios;
    private ScenarioParameters scenarioParameters;
    private ExplorationStrategyParameters explorationStrategyParameters;
    private TerminationConfig termination;
}
