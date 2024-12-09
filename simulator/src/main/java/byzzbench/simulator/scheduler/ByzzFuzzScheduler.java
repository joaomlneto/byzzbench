package byzzbench.simulator.scheduler;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.config.ByzzBenchConfig;
import byzzbench.simulator.faults.Fault;
import byzzbench.simulator.faults.FaultContext;
import byzzbench.simulator.faults.FaultFactory;
import byzzbench.simulator.faults.factories.ByzzFuzzScenarioFaultFactory;
import byzzbench.simulator.service.MessageMutatorService;
import com.fasterxml.jackson.databind.JsonNode;
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
    public void loadSchedulerParameters(JsonNode parameters) {
        System.out.println("Loading ByzzFuzz parameters:");

        if (parameters != null)
            System.out.println(parameters.toPrettyString());

        if (parameters != null && parameters.has("numRoundsWithProcessFaults")) {
            this.numRoundsWithProcessFaults = parameters.get("numRoundsWithProcessFaults").asInt();
        }

        if (parameters != null && parameters.has("numRoundsWithNetworkFaults")) {
            this.numRoundsWithNetworkFaults = parameters.get("numRoundsWithNetworkFaults").asInt();
        }

        if (parameters != null && parameters.has("numRoundsWithFaults")) {
            this.numRoundsWithFaults = parameters.get("numRoundsWithFaults").asInt();
        }
    }

    @Override
    public void initializeScenario(Scenario scenario) {
        super.initializeScenario(scenario);

        // Generate round-aware small-scope mutations for the scenario
        FaultFactory faultFactory = new ByzzFuzzScenarioFaultFactory();
        FaultContext context = new FaultContext(scenario);
        List<Fault> faults = faultFactory.generateFaults(context);
        faults.forEach(fault -> scenario.getTransport().addFault(fault, true));
    }

    @Override
    public int dropMessageWeight(Scenario scenario) {
        // ByzzFuzz does not drop messages as a scheduler decision
        return 0;
    }

    @Override
    public int mutateMessageWeight(Scenario scenario) {
        // ByzzFuzz does not mutate messages as a scheduler decision
        return 0;
    }

    @Override
    public void reset() {
        // nothing to do
    }
}
