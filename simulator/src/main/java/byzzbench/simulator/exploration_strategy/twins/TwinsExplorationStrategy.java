package byzzbench.simulator.exploration_strategy.twins;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.exploration_strategy.ExplorationStrategyParameters;
import byzzbench.simulator.exploration_strategy.random.RandomExplorationStrategy;
import byzzbench.simulator.nodes.Replica;
import lombok.Getter;
import lombok.extern.java.Log;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Twins exploration_strategy from "Twins: BFT Systems Made Robust" by Shehar Bano,
 * Alberto Sonnino, Andrey Chursin, Dmitri Perelman, Zekun Li, Avery Ching and
 * Dahlia Malkhi.
 * https://drops.dagstuhl.de/entities/document/10.4230/LIPIcs.OPODIS.2021.7
 */
@Component
@Log
@Getter
public class TwinsExplorationStrategy extends RandomExplorationStrategy {
    /**
     * Scenario-specific strategy data
     */
    private final Map<Scenario, TwinsScenarioStrategyData> scenarioData = new HashMap<>();

    @Override
    public void initializeScenario(Scenario scenario) {
        ExplorationStrategyParameters config = scenario.getSchedule().getCampaign().getExplorationStrategyParameters();

        TwinsScenarioStrategyData scenarioParams = TwinsScenarioStrategyData.builder()
                .numReplicas(Integer.parseInt(config.getParams().get("numReplicas")))
                .numTwinsPerReplica(Integer.parseInt(config.getParams().get("numTwinsPerReplica")))
                .numRounds(Integer.parseInt(config.getParams().get("numRounds")))
                .build();

        this.scenarioData.put(scenario, scenarioParams);

        // Get the IDs of the replicas
        List<String> replicaIds = scenario.getFaultyReplicaIds().stream().toList();

        if (replicaIds.size() < scenarioParams.getNumReplicas()) {
            throw new IllegalArgumentException("Not enough replicas to create " + scenarioParams.getNumReplicas() + " twins");
        }

        replicaIds = replicaIds.subList(0, scenarioParams.getNumReplicas());
        // Create the twins for each replica
        for (int i = 0; i < scenarioParams.getNumReplicas(); i++) {
            Replica replica = scenario.getReplicas().get(replicaIds.get(i));
            log.info("Creating " + scenarioParams.getNumTwinsPerReplica() + " twins for replica " + replica.getId());
            scenario.getNodes().put(replica.getId(), new TwinsReplica(replica, scenarioParams.getNumTwinsPerReplica(), scenarioParams.getNumRounds()));
        }
    }

}
