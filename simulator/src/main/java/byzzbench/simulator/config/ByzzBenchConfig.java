package byzzbench.simulator.config;

import byzzbench.simulator.exploration_strategy.ExplorationStrategyParameters;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
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
     * Policy for saving schedules in the database.
     */
    private SaveScheduleMode saveSchedules = SaveScheduleMode.ALL;
    /**
     * Scheduler parameters.
     */
    private ExplorationStrategyParameters scheduler = new ExplorationStrategyParameters();

    /**
     * List of campaign configurations to run at startup.
     */
    private List<CampaignConfig> campaigns = new ArrayList<>();

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
}
