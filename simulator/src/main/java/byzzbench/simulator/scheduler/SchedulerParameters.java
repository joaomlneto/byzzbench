package byzzbench.simulator.scheduler;

import byzzbench.simulator.config.ByzzBenchConfig;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class SchedulerParameters {
    private final ExecutionMode executionMode = ExecutionMode.SYNC;
    private String id;
    private int maxDropMessages = 0;
    private int maxMutateMessages = 0;
    private int deliverTimeoutWeight = 1;
    private int deliverMessageWeight = 99;
    private int dropMessageWeight = 0;
    private int mutateMessageWeight = 0;
    private Map<String, String> params = new HashMap<>();
    private List<ByzzBenchConfig.FaultConfig> faults = new ArrayList<>();
    private List<ByzzBenchConfig.FaultConfig> mutations = new ArrayList<>();

    public enum ExecutionMode {
        /**
         * The scheduler will deliver any message that is currently queued.
         * This is the default behavior.
         */
        ASYNC,
        /**
         * The scheduler will deliver the earliest-sent message that is currently queued.
         * This follows the communication-closure hypothesis.
         * This should be used with a non-zero "dropMessageWeight" to emulate the behavior in ByzzFuzz.
         */
        SYNC,
    }
}
