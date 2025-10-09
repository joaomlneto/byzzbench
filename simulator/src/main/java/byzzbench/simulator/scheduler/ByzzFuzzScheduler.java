package byzzbench.simulator.scheduler;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.config.ByzzBenchConfig;
import byzzbench.simulator.faults.Fault;
import byzzbench.simulator.faults.FaultFactory;
import byzzbench.simulator.faults.ScenarioContext;
import byzzbench.simulator.faults.factories.ByzzFuzzScenarioFaultFactory;
import byzzbench.simulator.service.MessageMutatorService;
import lombok.Getter;
import lombok.extern.java.Log;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * The ByzzFuzz scheduler from "Randomized Testing of Byzantine Fault Tolerant Algorithms" by
 * Levin N. Winter, Florena Buse, Daan de Graaf, Klaus von Gleissenthall, and Burcu Kulahcioglu Ozkan.
 * https://dl.acm.org/doi/10.1145/3586053
 */
@Component
@Log
@Getter
public class ByzzFuzzScheduler extends RandomScheduler {
    /**
     * Small-scope mutations to be applied to protocol messages
     */
    private final List<Fault> mutations = new ArrayList<>();
    /**
     * Random number generator
     */
    private final Random random = new Random();
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

    public ByzzFuzzScheduler(ByzzBenchConfig config, MessageMutatorService messageMutatorService) {
        super(config, messageMutatorService);
    }

    @Override
    public String getId() {
        return "ByzzFuzz";
    }

    @Override
    public void loadSchedulerParameters(SchedulerParameters parameters) {
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
    }

}
