package byzzbench.simulator.config;

import byzzbench.simulator.SimulatorApplication;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuration for the ByzzBench simulator.
 */
@Component
@ConfigurationProperties("byzzbench")
@Data
@Validated
public class ByzzBenchConfig {
    /**
     * Whether to start generating scenarios on startup automatically.
     */
    private boolean autostart = false;

    /**
     * The number of scenarios to run at a time. Defaults to Integer.MAX_VALUE.
     */
    private int numScenarios = Integer.MAX_VALUE;

    /**
     * The path to the output directory. Defaults to "./output".
     */
    private Path outputPath = Path.of("output");

    /**
     * Scheduler parameters.
     */
    private SchedulerConfig scheduler = new SchedulerConfig();
    /**
     * Scenario parameters.
     */
    private ScenarioConfig scenario = new ScenarioConfig();

    /**
     * Configuration for the termination
     */
    @Data
    public static final class TerminationConfig {
        /**
         * The minimum number of events to run before termination. 0 means no minimum.
         */
        private long minEvents = 0;
        /**
         * The minimum number of rounds to run before termination. 0 means no minimum.
         */
        private long minRounds = 2;
        /**
         * Frequency of checking termination conditions.
         * Setting it to "1" means check every round, 2 means check every other round, etc.
         * The default is 1 (check every round).
         */
        private long samplingFrequency = 1;
    }

    /**
     * Configuration for the scheduler component.
     */
    @Data
    public final class SchedulerConfig {
        /**
         * The ID of the scheduler to use.
         */
        private String id;
        /**
         * Execution mode for the scheduler, limiting which messages can be delivered as the next scheduler action.
         */
        private ExecutionMode executionMode = ExecutionMode.ASYNC;
        /**
         * Maximum number of messages to drop for a given scenario.
         */
        private int maxDropMessages = 0;
        /**
         * Maximum number of messages to mutate-and-deliver for a given scenario.
         */
        private int maxMutateMessages = 0;
        /**
         * Weighted probability of triggering a timeout
         */
        private int deliverTimeoutWeight = 1;
        /**
         * Weighted probability of delivering a message
         */
        private int deliverMessageWeight = 99;
        /**
         * Weighted probability of delivering a request from a client
         */
        private int deliverClientRequestWeight = 99;
        /**
         * Weighted probability of dropping a message.
         * The default is 0 (no messages dropped as a scheduler decision).
         */
        private int dropMessageWeight = 0;
        /**
         * Weighted probability of mutating and delivering a message.
         * The default is 0 (no messages are mutated as a scheduler decision).
         */
        private int mutateMessageWeight = 0;
        private Map<String, String> params;
        private List<FaultConfig> faults = new ArrayList<>();
        private List<FaultConfig> mutations = new ArrayList<>();

        // execution should be either "async" or "sync". Here is the enum:
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

    /**
     * Configuration for a fault or mutation.
     * A fault is identified by a predicate and a behavior.
     */
    @Data
    public final class FaultConfig {
        private final PredicateConfig predicate;
        private final BehaviorConfig behavior;
    }

    /**
     * Configuration for a predicate.
     */
    @Data
    public final class PredicateConfig {
        private final String id;
        private final Map<String, String> params;
    }

    /**
     * Configuration for a behavior.
     */
    @Data
    public final class BehaviorConfig {
        private final String id;
        private final Map<String, String> params;
    }

    /**
     * Configuration for the scenario component.
     */
    @Data
    public final class ScenarioConfig {
        private TerminationConfig termination = new TerminationConfig();
        private String id = "pbft-java";
        private Map<String, String> params = new HashMap<>();
    }
}
