package byzzbench.simulator.faults;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.faults.behaviors.MutateMessageBehavior;
import byzzbench.simulator.faults.predicates.MessageRecipientHasIdPredicate;
import byzzbench.simulator.faults.predicates.MessageSenderHasIdPredicate;
import byzzbench.simulator.faults.predicates.RoundPredicate;
import byzzbench.simulator.scheduler.ByzzFuzzScheduler;
import byzzbench.simulator.utils.SetSubsets;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Fault factory that generates faults for a given scenario.
 */
public class ByzzFuzzScenarioFaultFactory implements FaultFactory {
    Set<Scenario> processedScenarios = new HashSet<>();

    @Override
    public List<Fault> generateFaults(FaultContext input) {
        List<Fault> faults = new ArrayList<>();

        Scenario scenario = input.getScenario();
        // Check if the scenario has already been processed
        if (processedScenarios.contains(scenario)) {
            throw new IllegalStateException("Scenario already processed!");
        }

        // Add the scenario to the processed set
        processedScenarios.add(scenario);

        // assert the scenario is configured with a byzzfuzz scheduler
        if (!(scenario.getScheduler() instanceof ByzzFuzzScheduler scheduler)) {
            throw new IllegalArgumentException("Scenario scheduler must be a ByzzFuzzScheduler");
        }

        // get params
        int c = scheduler.getNumRoundsWithProcessFaults();
        int d = scheduler.getNumRoundsWithNetworkFaults();
        int r = scheduler.getNumRoundsWithFaults();

        Set<String> nodeIds = scenario.getNodes().keySet();

        // Create network faults
        for (int i = 1; i <= d; i++) {
            // get random element from [1, r]
            int round = (int) (Math.random() * r) + 1;
            // random partition of nodeIds
            // FIXME: this is not a uniform distribution
            Set<String> partition = nodeIds.stream().filter(n -> Math.random() < 0.5).collect(Collectors.toSet());
            // generate network fault
            Fault networkFault = new ByzzFuzzNetworkFault(partition, round);
            faults.add(networkFault);
        }

        // Create process faults
        for (int i = 1; i < c; i++) {
            // get random element from [1, r]
            int round = (int) (Math.random() * r) + 1;
            // random node
            // FIXME: this is not a uniform distribution
            String senderId = nodeIds.stream().skip((int) (Math.random() * nodeIds.size())).findFirst().orElseThrow();
            Set<String> recipientIds = SetSubsets.getRandomSubset(nodeIds);
            // generate process fault
            FaultPredicate matchesRound = new RoundPredicate(round);
            FaultPredicate matchesSenderId = new MessageSenderHasIdPredicate(senderId);
            FaultPredicate matchesRecipientIds = new MessageRecipientHasIdPredicate(recipientIds);
            FaultBehavior behavior = new MutateMessageBehavior();
            Fault processFault = new BaseFault(
                    "byzzfuzz-process-fault-" + i,
                    matchesRound.and(matchesSenderId).and(matchesRecipientIds),
                    behavior);
            faults.add(processFault);
        }

        return faults;
    }
}
