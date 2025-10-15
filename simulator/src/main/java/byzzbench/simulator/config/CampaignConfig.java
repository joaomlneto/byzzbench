package byzzbench.simulator.config;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CampaignConfig {
    private long initialRandomSeed;
    private String scenarioId;
    private String explorationStrategyId;
    private long numScenarios;
}
