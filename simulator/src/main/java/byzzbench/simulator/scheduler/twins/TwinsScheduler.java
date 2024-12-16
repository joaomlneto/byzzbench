package byzzbench.simulator.scheduler.twins;

import byzzbench.simulator.Replica;
import byzzbench.simulator.Scenario;
import byzzbench.simulator.config.ByzzBenchConfig;
import byzzbench.simulator.scheduler.RandomScheduler;
import byzzbench.simulator.service.MessageMutatorService;
import lombok.Getter;
import lombok.extern.java.Log;
import org.springframework.stereotype.Component;

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
    public TwinsScheduler(ByzzBenchConfig config, MessageMutatorService messageMutatorService) {
        super(config, messageMutatorService);
    }

    @Override
    public String getId() {
        return "Twins";
    }

    @Override
    public void initializeScenario(Scenario scenario) {
        int numReplicas = 2;
        Replica replica = scenario.getReplicas().firstEntry().getValue();
        log.info("Creating " + numReplicas + " twins for replica " + replica.getId());
        scenario.getNodes().put(replica.getId(), new TwinsReplica(replica, numReplicas));

        // print replica class names
        scenario.getReplicas().forEach((id, r) -> log.info("Replica " + id + " is of class " + r.getClass().getName()));
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
