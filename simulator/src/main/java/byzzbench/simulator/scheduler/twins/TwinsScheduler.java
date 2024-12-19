package byzzbench.simulator.scheduler.twins;

import byzzbench.simulator.Replica;
import byzzbench.simulator.Scenario;
import byzzbench.simulator.config.ByzzBenchConfig;
import byzzbench.simulator.scheduler.RandomScheduler;
import byzzbench.simulator.service.MessageMutatorService;
import lombok.Getter;
import lombok.extern.java.Log;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * The Twins scheduler from "Twins: BFT Systems Made Robust" by Shehar Bano,
 * Alberto Sonnino, Andrey Chursin, Dmitri Perelman, Zekun Li, Avery Ching and
 * Dahlia Malkhi.
 * https://drops.dagstuhl.de/entities/document/10.4230/LIPIcs.OPODIS.2021.7
 */
@Component
@Log
@Getter
public class TwinsScheduler extends RandomScheduler {
    /**
     * The number of replicas to create twins for.
     */
    private final int numReplicas;

    /**
     * The number of twins to create for each replica.
     */
    private final int numTwinsPerReplica;

    public TwinsScheduler(ByzzBenchConfig config, MessageMutatorService messageMutatorService) {
        super(config, messageMutatorService);
        Map<String, String> schedulerParams = config.getScheduler().getParams();
        this.numReplicas = Integer.parseInt(schedulerParams.getOrDefault("numReplicas", "1"));
        this.numTwinsPerReplica = Integer.parseInt(schedulerParams.getOrDefault("numTwinsPerReplica", "2"));
    }

    @Override
    public String getId() {
        return "Twins";
    }

    @Override
    public void initializeScenario(Scenario scenario) {
        // Get the IDs of the replicas
        List<String> replicaIds = scenario.getReplicas().keySet().stream().sorted().toList();

        if (replicaIds.size() < numReplicas) {
            throw new IllegalArgumentException("Not enough replicas to create " + numReplicas + " twins");
        }

        replicaIds = replicaIds.subList(0, numReplicas);
        // Create the twins for each replica
        for (int i = 0; i < numReplicas; i++) {
            Replica replica = scenario.getReplicas().get(replicaIds.get(i));
            log.info("Creating " + numTwinsPerReplica + " twins for replica " + replica.getId());
            scenario.getNodes().put(replica.getId(), new TwinsReplica(replica, numTwinsPerReplica));
        }
    }

    @Override
    public int dropMessageWeight(Scenario scenario) {
        // Twins does not drop messages as a scheduler decision
        return 0;
    }

    @Override
    public int mutateMessageWeight(Scenario scenario) {
        // Twins does not mutate messages as a scheduler decision
        return 0;
    }

}
