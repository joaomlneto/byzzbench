package byzzbench.simulator.exploration_strategy.twins;

import byzzbench.simulator.Replica;
import byzzbench.simulator.Scenario;
import byzzbench.simulator.config.ByzzBenchConfig;
import byzzbench.simulator.exploration_strategy.random.RandomExplorationStrategy;
import lombok.Getter;
import lombok.extern.java.Log;
import org.springframework.stereotype.Component;

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

    public TwinsExplorationStrategy(ByzzBenchConfig config) {
        super(config);
        Map<String, String> schedulerParams = config.getScheduler().getParams();
        this.numReplicas = Integer.parseInt(schedulerParams.getOrDefault("numReplicas", "1"));
        this.numTwinsPerReplica = Integer.parseInt(schedulerParams.getOrDefault("numTwinsPerReplica", "2"));
        this.numRounds = Integer.parseInt(schedulerParams.getOrDefault("numRounds", "1"));
    }

    @Override
    public String getId() {
        return "Twins";
    }

    @Override
    public void initializeScenario(Scenario scenario) {
        // Get the IDs of the replicas
        List<String> replicaIds = scenario.getFaultyReplicaIds().stream().toList();

        if (replicaIds.size() < numReplicas) {
            throw new IllegalArgumentException("Not enough replicas to create " + numReplicas + " twins");
        }

        replicaIds = replicaIds.subList(0, numReplicas);
        // Create the twins for each replica
        for (int i = 0; i < numReplicas; i++) {
            Replica replica = scenario.getReplicas().get(replicaIds.get(i));
            log.info("Creating " + numTwinsPerReplica + " twins for replica " + replica.getId());
            scenario.getNodes().put(replica.getId(), new TwinsReplica(replica, numTwinsPerReplica, numRounds));
        }
    }

}
