package byzzbench.simulator.exploration_strategy.byzzfuzz;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.config.ByzzBenchConfig;
import byzzbench.simulator.domain.Action;
import byzzbench.simulator.domain.DeliverMessageAction;
import byzzbench.simulator.domain.FaultInjectionAction;
import byzzbench.simulator.exploration_strategy.ExplorationStrategyParameters;
import byzzbench.simulator.exploration_strategy.ScenarioStrategyData;
import byzzbench.simulator.exploration_strategy.random.RandomExplorationStrategy;
import byzzbench.simulator.faults.Fault;
import byzzbench.simulator.faults.FaultFactory;
import byzzbench.simulator.faults.ScenarioContext;
import byzzbench.simulator.faults.factories.ByzzFuzzScenarioFaultFactory;
import byzzbench.simulator.transport.Event;
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
     * Scenario-specific strategy data
     */
    private final Map<Scenario, ByzzFuzzScenarioStrategyData> scenarioData = new HashMap<>();

    /**
     * Small-scope mutations to be applied to protocol messages
     */
    private final List<Fault> mutations = new ArrayList<>();
    /**
     * Number of protocol rounds with process faults
     */
    @Getter
    private int numRoundsWithProcessFaults = 1;
    /**
     * Number of protocol rounds with network faults
     */
    @Getter
    private int numRoundsWithNetworkFaults = 1;
    /**
     * Number of protocol rounds among which the faults will be injected
     */
    @Getter
    private int numRoundsWithFaults = 3;

    public ByzzFuzzExplorationStrategy(ByzzBenchConfig config) {
        super(config);
    }

    @Override
    public String getId() {
        return "ByzzFuzz";
    }

    @Override
    public void loadSchedulerParameters(ExplorationStrategyParameters parameters) {
        System.out.println("Loading ByzzFuzz parameters:");

        if (parameters != null) {
            System.out.println(parameters);
        }

        if (parameters != null && parameters.getParams().containsKey("numRoundsWithProcessFaults")) {
            this.numRoundsWithProcessFaults = Integer.parseInt(parameters.getParams().get("numRoundsWithProcessFaults"));
        }

        if (parameters != null && parameters.getParams().containsKey("numRoundsWithNetworkFaults")) {
            this.numRoundsWithNetworkFaults = Integer.parseInt(parameters.getParams().get("numRoundsWithNetworkFaults"));
        }

        if (parameters != null && parameters.getParams().containsKey("numRoundsWithFaults")) {
            this.numRoundsWithFaults = Integer.parseInt(parameters.getParams().get("numRoundsWithFaults"));
        }
    }

    /**
     * Get or create scenario-specific strategy data
     *
     * @param scenario the scenario
     * @return the scenario-specific strategy data
     */
    public ByzzFuzzScenarioStrategyData getScenarioData(Scenario scenario) {
        if (!(scenario instanceof ByzzFuzzScenario byzzFuzzScenario)) {
            throw new UnsupportedOperationException("Scenario is not a ByzzFuzzScenario");
        }

        return this.scenarioData.computeIfAbsent(scenario, s -> ByzzFuzzScenarioStrategyData.builder()
                .remainingDropMessages(this.getConfig().getScheduler().getMaxDropMessages())
                .remainingMutateMessages(this.getConfig().getScheduler().getMaxMutateMessages())
                .initializedByStrategy(this.getInitializedScenarios().contains(scenario))
                .numRoundsWithProcessFaults(this.getNumRoundsWithProcessFaults())
                .numRoundsWithNetworkFaults(this.getNumRoundsWithNetworkFaults())
                .numRoundsWithFaults(this.getNumRoundsWithFaults())
                .faults(new ArrayList<>())
                .replicaRounds(byzzFuzzScenario.getRoundInfoOracle().getReplicaRounds())
                .roundInfos(byzzFuzzScenario.getRoundInfoOracle().getReplicasRoundInfo())
                .build());
    }

    @Override
    public void initializeScenario(Scenario scenario) {
        if (!(scenario instanceof ByzzFuzzScenario)) {
            throw new UnsupportedOperationException("Scenario is not a ByzzFuzzScenario");
        }

        // Generate round-aware small-scope mutations for the scenario
        FaultFactory faultFactory = new ByzzFuzzScenarioFaultFactory();
        ScenarioContext context = new ScenarioContext(scenario);
        List<Fault> faults = faultFactory.generateFaults(context);
        // FIXME: this is old behavior
        //faults.forEach(fault -> scenario.getTransport().addFault(fault, true));

        log.fine("ByzzFuzz initialized scenario with " + faults.size() + " faults.");
        this.getScenarioData(scenario).getFaults().addAll(faults);
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

        List<Fault> faults = this.getScenarioData(scenario).getFaults();
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
        List<Fault> faults = this.getScenarioData(scenario).getFaults();

        // if one of the faults can be applied to the message, apply it
        for (Event messageEvent : this.getQueuedMessageEvents(scenario)) {
            ScenarioContext context = new ScenarioContext(scenario, messageEvent);
            for (Fault fault : faults) {
                if (fault.test(context)) {
                    return List.of(FaultInjectionAction.fromEvent(fault));
                }
            }
        }

        return super.getAvailableActions(scenario).stream().filter(a -> !(a instanceof FaultInjectionAction)).toList();
    }

    @Override
    public ScenarioStrategyData getScenarioStrategyData(Scenario scenario) {
        ByzzFuzzScenarioStrategyData data = this.getScenarioData(scenario);
        return ByzzFuzzScenarioStrategyData.builder()
                .remainingDropMessages(data.getRemainingDropMessages())
                .remainingMutateMessages(data.getRemainingMutateMessages())
                .initializedByStrategy(this.getInitializedScenarios().contains(scenario))
                .numRoundsWithProcessFaults(data.getNumRoundsWithProcessFaults())
                .numRoundsWithNetworkFaults(data.getNumRoundsWithNetworkFaults())
                .numRoundsWithFaults(data.getNumRoundsWithFaults())
                .faults(data.getFaults())
                .roundInfos(data.getRoundInfos())
                .replicaRounds(data.getReplicaRounds())
                .build();
    }
}
