package byzzbench.simulator.exploration_strategy;

import byzzbench.simulator.ExecutionMode;
import byzzbench.simulator.config.FaultConfig;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class ExplorationStrategyParameters implements Serializable {
    private String explorationStrategyId;
    private ExecutionMode executionMode;
    private long randomSeed;
    private int maxMutationsPerMessage = 2;
    private int maxDropMessages = 0;
    private int maxMutateMessages = 0;
    private int deliverTimeoutWeight = 1;
    private int deliverMessageWeight = 99;
    private int dropMessageWeight = 0;
    private int mutateMessageWeight = 0;
    private transient Map<String, String> params = new HashMap<>();
    private List<FaultConfig> faults = new ArrayList<>();
    private transient List<FaultConfig> mutations = new ArrayList<>();

}
