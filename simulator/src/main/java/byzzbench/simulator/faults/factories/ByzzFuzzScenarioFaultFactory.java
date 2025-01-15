package byzzbench.simulator.faults.factories;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.faults.Fault;
import byzzbench.simulator.faults.FaultContext;
import byzzbench.simulator.faults.FaultFactory;
import byzzbench.simulator.faults.faults.ByzzFuzzNetworkFault;
import byzzbench.simulator.faults.faults.ByzzFuzzProcessFault;
import byzzbench.simulator.scheduler.ByzzFuzzScheduler;
import byzzbench.simulator.utils.SetSubsets;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Fault factory that generates faults for a given scenario.
 */
@Component
public class ByzzFuzzScenarioFaultFactory implements FaultFactory {
    private final Random rand = new Random(2137L);

    @Override
    public List<Fault> generateFaults(FaultContext input) {
        List<Fault> faults = new ArrayList<>();
        Scenario scenario = input.getScenario();

        // assert the scenario is configured with a byzzfuzz scheduler
        if (!(scenario.getScheduler() instanceof ByzzFuzzScheduler scheduler)) {
            throw new IllegalArgumentException("Scenario scheduler must be a ByzzFuzzScheduler");
        }

        // get scheduler params
        int c = scheduler.getNumRoundsWithProcessFaults();
        int d = scheduler.getNumRoundsWithNetworkFaults();
        int r = scheduler.getNumRoundsWithFaults();
        Set<String> replicaIds = scenario.getReplicas().keySet();

        // Create network faults
        for (int i = 1; i <= d; i++) {
            int round = rand.nextInt(r) + 1;
            Set<String> partition = SetSubsets.getRandomNonEmptySubset(replicaIds);
            Fault networkFault = new ByzzFuzzNetworkFault(partition, round);
            faults.add(networkFault);
        }

        // Create process faults
        for (int i = 1; i <= c; i++) {
            int round = rand.nextInt(r) + 1;
            String sender = replicaIds.stream().skip(rand.nextInt(replicaIds.size())).findFirst().orElseThrow();
            Set<String> recipientIds = SetSubsets.getRandomNonEmptySubset(replicaIds);

            // generate process fault
            Fault processFault = new ByzzFuzzProcessFault(recipientIds, sender, round);
            faults.add(processFault);
        }

        // Faults
        System.out.println("ByzzFuzzFaults:");
        faults.forEach(fault -> System.out.println(fault.getId()));

        return faults;
    }
}
