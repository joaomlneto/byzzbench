package byzzbench.simulator.exploration_strategy.twins;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.exploration_strategy.ExplorationStrategyParameters;
import byzzbench.simulator.exploration_strategy.random.RandomExplorationStrategy;
import byzzbench.simulator.nodes.Replica;
import byzzbench.simulator.utils.StirlingNumberSecondKind;
import lombok.Getter;
import lombok.extern.java.Log;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

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

        // Get the IDs of the replicas to compromise (turn into twins)
        List<String> replicaIds = scenario.getFaultyReplicaIds().stream().toList();

        if (replicaIds.size() < scenarioParams.getNumReplicas()) {
            throw new IllegalArgumentException("Not enough replicas to create " + scenarioParams.getNumReplicas() + " twins");
        }

        replicaIds = replicaIds.subList(0, scenarioParams.getNumReplicas());

        // 1. Create the twin instances for each compromised replica and replace
        //    the original in the node set with the multiplexing TwinsReplica.
        for (String replicaId : replicaIds) {
            Replica replica = scenario.getReplicas().get(replicaId);
            log.info("Creating " + scenarioParams.getNumTwinsPerReplica() + " twins for replica " + replica.getId());
            scenario.getNodes().put(replica.getId(), new TwinsReplica(replica, scenarioParams.getNumTwinsPerReplica()));
        }

        // 2. Build the GLOBAL partition universe: honest replicas by their id,
        //    twin instances by their internal addresses, so that a node and its
        //    twin can be placed in different partitions.
        List<String> universe = new ArrayList<>();
        for (Replica replica : scenario.getReplicas().values()) {
            if (replica instanceof TwinsReplica twin) {
                universe.addAll(twin.getInternalIds());
            } else {
                universe.add(replica.getId());
            }
        }

        // 3. Generate ONE global per-round partition schedule, shared by all
        //    nodes. Partitions are sampled (using the scenario's seeded RNG, for
        //    reproducibility) from all the ways of splitting the universe into
        //    1, 2 or 3 parts.
        List<List<List<String>>> options = new ArrayList<>();
        options.addAll(StirlingNumberSecondKind.getPartitions(universe, 1));
        options.addAll(StirlingNumberSecondKind.getPartitions(universe, 2));
        options.addAll(StirlingNumberSecondKind.getPartitions(universe, 3));

        Random rand = new Random(scenario.getRandom().nextLong());
        Map<Long, List<List<String>>> roundPartitions = new TreeMap<>();
        for (long round = 0; round < scenarioParams.getNumRounds(); round++) {
            roundPartitions.put(round, options.get(rand.nextInt(options.size())));
        }

        // 4. Create the single global Twins transport and wire EVERY replica to
        //    it (honest replicas and twin instances), so the same partition
        //    schedule governs all links, including honest-to-honest ones.
        TwinsTransport transport = new TwinsTransport(scenario, roundPartitions, universe);
        for (Replica replica : scenario.getReplicas().values()) {
            if (replica instanceof TwinsReplica twin) {
                twin.setTwinsTransport(transport);
                twin.setTransport(transport);
                twin.getReplicas().forEach(instance -> instance.setTransport(transport));
            } else {
                replica.setTransport(transport);
            }
        }

        roundPartitions.forEach((round, partitions) -> log.info("Round " + round + ": " + partitions));
    }

}
