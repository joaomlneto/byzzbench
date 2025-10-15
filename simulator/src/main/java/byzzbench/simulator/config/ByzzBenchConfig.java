package byzzbench.simulator.config;

import byzzbench.simulator.exploration_strategy.ExplorationStrategyParameters;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.HashMap;
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
     * Policy for saving schedules in the database.
     */
    private SaveScheduleMode saveSchedules = SaveScheduleMode.ALL;
    /**
     * Scheduler parameters.
     */
    private ExplorationStrategyParameters scheduler = new ExplorationStrategyParameters();
    /**
     * Scenario parameters.
     */
    private ScenarioConfig scenario = new ScenarioConfig();
    
    public enum SaveScheduleMode {
        /**
         * Save all schedules in the database.
         */
        ALL,
        /**
         * Save only schedules that did not terminate successfully.
         */
        BUGGY,
        /**
         * Do not save any schedules.
         */
        NONE,
    }

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
