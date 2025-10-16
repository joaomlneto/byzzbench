package byzzbench.simulator.exploration_strategy.byzzfuzz;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.config.ByzzBenchConfig;
import byzzbench.simulator.exploration_strategy.ExplorationStrategyParameters;
import byzzbench.simulator.exploration_strategy.ScenarioStrategyData;
import byzzbench.simulator.exploration_strategy.random.RandomExplorationStrategy;
import byzzbench.simulator.faults.Fault;
import byzzbench.simulator.faults.FaultFactory;
import byzzbench.simulator.faults.ScenarioContext;
import byzzbench.simulator.faults.factories.ByzzFuzzScenarioFaultFactory;
import lombok.Getter;
import lombok.extern.java.Log;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The ByzzFuzz exploration_strategy from "Randomized Testing of Byzantine Fault Tolerant Algorithms" by
 * Levin N. Winter, Florena Buse, Daan de Graaf, Klaus von Gleissenthall, and Burcu Kulahcioglu Ozkan.
 * https://dl.acm.org/doi/10.1145/3586053
 */
@Component
@Log
@Getter
public class ByzzFuzzExplorationStrategy extends RandomExplorationStrategy {
    /**
     * Map of faults per scenario (to avoid generating them multiple times)
     */
    private final Map<Scenario, List<Fault>> scenarioFaults = new HashMap<>();
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

    @Override
    public void initializeScenario(Scenario scenario) {
        // Generate round-aware small-scope mutations for the scenario
        FaultFactory faultFactory = new ByzzFuzzScenarioFaultFactory();
        ScenarioContext context = new ScenarioContext(scenario);
        List<Fault> faults = faultFactory.generateFaults(context);
        faults.forEach(fault -> scenario.getTransport().addFault(fault, true));

        this.scenarioFaults.put(scenario, faults);
    }

    @Override
    public ScenarioStrategyData getScenarioStrategyData(Scenario scenario) {
        return ByzzFuzzScenarioStrategyData.builder()
                .remainingDropMessages(this.getConfig().getScheduler().getMaxDropMessages())
                .remainingMutateMessages(this.getConfig().getScheduler().getMaxMutateMessages())
                .initializedByStrategy(this.getInitializedScenarios().contains(scenario))
                .numRoundsWithProcessFaults(this.getNumRoundsWithProcessFaults())
                .numRoundsWithNetworkFaults(this.getNumRoundsWithNetworkFaults())
                .numRoundsWithFaults(this.getNumRoundsWithFaults())
                .faults(this.scenarioFaults.get(scenario))
                .build();
    }
}
