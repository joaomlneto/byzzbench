package byzzbench.simulator.exploration_strategy.byzzfuzz;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.domain.Action;
import byzzbench.simulator.domain.DeliverMessageAction;
import byzzbench.simulator.domain.FaultInjectionAction;
import byzzbench.simulator.exploration_strategy.ExplorationStrategyParameters;
import byzzbench.simulator.exploration_strategy.ScenarioStrategyData;
import byzzbench.simulator.exploration_strategy.random.RandomExplorationStrategy;
import byzzbench.simulator.faults.Fault;
import byzzbench.simulator.faults.ScenarioContext;
import byzzbench.simulator.faults.faults.ByzzFuzzNetworkFault;
import byzzbench.simulator.faults.faults.ByzzFuzzProcessFault;
import byzzbench.simulator.transport.Event;
import byzzbench.simulator.utils.SetSubsets;
import lombok.Getter;
import lombok.extern.java.Log;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * The ByzzFuzz exploration_strategy from "Randomized Testing of Byzantine Fault Tolerant Algorithms" by
 * Levin N. Winter, Florena Buse, Daan de Graaf, Klaus von Gleissenthall, and Burcu Kulahcioglu Ozkan.
 * <p>
 * <a href="https://dl.acm.org/doi/10.1145/3586053">Link to publication</a>
 */
@Component
@Log
@Getter
public class ByzzFuzzExplorationStrategy extends RandomExplorationStrategy {
    /**
     * Scenario-specific oracle
     */
    private final Map<Scenario, ByzzFuzzRoundInfoOracle> scenarioOracle = new HashMap<>();

    /**
     * Scenario-specific faults
     */
    private final Map<Scenario, List<Fault>> scenarioFaults = new HashMap<>();

    /**
     * Number of protocol rounds with process faults
     */
    @Getter
    private int numRoundsWithProcessFaults;

    /**
     * Number of protocol rounds with network faults
     */
    @Getter
    private int numRoundsWithNetworkFaults;

    /**
     * Number of protocol rounds among which the faults will be injected
     */
    @Getter
    private int numRoundsWithFaults;

    @Override
    public void loadSchedulerParameters(ExplorationStrategyParameters parameters) {
        if (parameters == null) {
            throw new IllegalArgumentException("parameters are null");
        }

        if (parameters.getParams().containsKey("numRoundsWithProcessFaults")) {
            this.numRoundsWithProcessFaults = Integer.parseInt(parameters.getParams().get("numRoundsWithProcessFaults"));
        }

        if (parameters.getParams().containsKey("numRoundsWithNetworkFaults")) {
            this.numRoundsWithNetworkFaults = Integer.parseInt(parameters.getParams().get("numRoundsWithNetworkFaults"));
        }

        if (parameters.getParams().containsKey("numRoundsWithFaults")) {
            this.numRoundsWithFaults = Integer.parseInt(parameters.getParams().get("numRoundsWithFaults"));
        }
    }

    @Override
    public void initializeScenario(Scenario scenario) {
        if (!(scenario instanceof ByzzFuzzScenario byzzFuzzScenario)) {
            throw new UnsupportedOperationException("Scenario is not a ByzzFuzzScenario");
        }

        // Try to read parameters from the schedule/campaign; if unavailable, skip fault generation
        ExplorationStrategyParameters config = scenario.getSchedule().getCampaign().getExplorationStrategyParameters();
        if (config == null || config.getParams() == null
                || !config.getParams().containsKey("numRoundsWithProcessFaults")
                || !config.getParams().containsKey("numRoundsWithNetworkFaults")
                || !config.getParams().containsKey("numRoundsWithFaults")) {
            log.fine("ByzzFuzz initializeScenario: no campaign parameters found; skipping factory fault generation.");
            throw new IllegalArgumentException("Incorrect parameters!!");
        }

        // get exploration strategy params
        ByzzFuzzRoundInfoOracle oracle = byzzFuzzScenario.getRoundInfoOracle();
        int c = Integer.parseInt(config.getParams().get("numRoundsWithProcessFaults"));
        int d = Integer.parseInt(config.getParams().get("numRoundsWithNetworkFaults"));
        int r = Integer.parseInt(config.getParams().get("numRoundsWithFaults"));
        SortedSet<String> replicaIds = new TreeSet<>(scenario.getReplicas().keySet());
        SortedSet<String> faultyReplicaIds = new TreeSet<>(scenario.getFaultyReplicaIds());
        List<Fault> faults = new ArrayList<>();

        // Create network faults
        for (int i = 0; i < d; i++) {
            int round = this.getRand().nextInt(r) + 1;
            Set<String> partition = SetSubsets.getRandomNonEmptySubset(replicaIds, this.getRand());
            Fault networkFault = new ByzzFuzzNetworkFault(partition, round, oracle);
            faults.add(networkFault);
        }

        // Create process faults
        for (int i = 0; i < c; i++) {
            int round = this.getRand().nextInt(r) + 1;
            String sender = faultyReplicaIds.stream().skip(this.getRand().nextInt(faultyReplicaIds.size())).findFirst().orElseThrow();
            Set<String> recipientIds = SetSubsets.getRandomNonEmptySubset(replicaIds, this.getRand());
            Fault processFault = new ByzzFuzzProcessFault(recipientIds, sender, round);
            faults.add(processFault);
        }

        // Faults
        log.info("ByzzFuzz initialized scenario with " + faults.size() + " faults.");
        this.scenarioOracle.put(scenario, oracle);
        this.scenarioFaults.put(scenario, faults);
    }

    @Override
    public synchronized Optional<Action> scheduleNext(Scenario scenario) {
        // ensure scenario is initialized!
        this.ensureScenarioInitialized(scenario);

        // get next action from the random exploration strategy
        Optional<Action> action = super.scheduleNext(scenario);

        // if we are not delivering a message, return the action
        if (action.isEmpty() || !(action.get() instanceof DeliverMessageAction deliverAction)) {
            return action;
        }

        List<Fault> faults = this.scenarioFaults.get(scenario);
        Event messageEvent = scenario.getTransport().getEvent(deliverAction.getMessageEventId());
        ScenarioContext context = new ScenarioContext(scenario, messageEvent);

        // go through the faults
        for (Fault fault : faults) {
            if (fault.test(context)) {
                log.fine("ByzzFuzz applying fault " + fault.getName() + " to message event " + messageEvent.getEventId());
                // apply the fault to the message event
                fault.accept(context);
            }
        }

        // return the action
        return action;
    }

    @Override
    public List<Action> getAvailableActions(Scenario scenario) {
        // ensure scenario is initialized!
        this.ensureScenarioInitialized(scenario);

        // if some of the existing network faults can be applied to a queued message, do it!
        List<Fault> faults = this.scenarioFaults.get(scenario);

        // if one of the faults can be applied to the message, apply it
        for (Event messageEvent : this.getQueuedMessageEvents(scenario)) {
            ScenarioContext context = new ScenarioContext(scenario, messageEvent);
            for (Fault fault : faults) {
                System.out.println("checking if " + fault.getId() + " can be applied to event " + messageEvent.getEventId());
                if (fault.test(context)) {
                    System.out.println("yes!");
                    System.out.println(fault);
                    return List.of(fault.toAction(context));
                }
            }
        }


        return super.getAvailableActions(scenario)
                .stream()
                .filter(a -> !(a instanceof FaultInjectionAction))
                .toList();
    }

    @Override
    public ScenarioStrategyData getScenarioStrategyData(Scenario scenario) {
        return ByzzFuzzScenarioStrategyData.builder()
                .remainingDropMessages(super.getScenarioStrategyData(scenario).getRemainingDropMessages())
                .remainingMutateMessages(super.getScenarioStrategyData(scenario).getRemainingMutateMessages())
                .initializedByStrategy(this.getInitializedScenarios().contains(scenario))
                .roundInfos(this.scenarioOracle.get(scenario).getReplicasRoundInfo())
                .replicaRounds(this.scenarioOracle.get(scenario).getReplicaRounds())
                .faults(this.scenarioFaults.get(scenario))
                .messageRound(this.scenarioOracle.get(scenario).getMessageRounds())
                .build();
    }
}
