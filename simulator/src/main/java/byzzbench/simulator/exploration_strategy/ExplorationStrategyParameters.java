package byzzbench.simulator.exploration_strategy;

import byzzbench.simulator.config.ByzzBenchConfig;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class ExplorationStrategyParameters {
    private String id;
    private int maxMutationsPerMessage = 2;
    private int maxDropMessages = 0;
    private int maxMutateMessages = 0;
    private int deliverTimeoutWeight = 1;
    private int deliverMessageWeight = 99;
    private int dropMessageWeight = 0;
    private int mutateMessageWeight = 0;
    private Map<String, String> params = new HashMap<>();
    private List<ByzzBenchConfig.FaultConfig> faults = new ArrayList<>();
    private List<ByzzBenchConfig.FaultConfig> mutations = new ArrayList<>();

}
