package byzzbench.simulator.config;

import byzzbench.simulator.utils.NonNull;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration for the ByzzBench simulator.
 */
@Component
@ConfigurationProperties("byzzbench")
@Data
@Validated
public class ByzzBenchConfig {
  /**
   * Whether to start generating scenarios on startup.
   */
  private boolean generateScenariosOnStartup = false;

  /**
   * The path to the output directory. Defaults to "./output".
   */
  private Path outputPath = Path.of("output");
  /**
   * Scheduler parameters.
   */
  private SchedulerConfig scheduler;
  /**
   * Scenario parameters.
   */
  private ScenarioConfig scenario;

  /**
   * Configuration for the scheduler component.
   */
  @Data
  public static final class SchedulerConfig {
    private String id;
    private Map<String, String> params;

    private List<FaultConfig> faults = new ArrayList<>();
    private List<FaultConfig> mutations = new ArrayList<>();
  }

  /**
   * Configuration for the scenario component.
   */
  @Data
  public static final class ScenarioConfig {
    @NonNull private PredicateConfig termination;
    private String id = "pbft-java";
    private Map<String, String> params = new HashMap<>();
  }

  /**
   * Configuration for a fault or mutation.
   * A fault is identified by a predicate and a behavior.
   */
  @Data
  public static final class FaultConfig {
    private final PredicateConfig predicate;
    private final BehaviorConfig behavior;
  }

  /**
   * Configuration for a predicate.
   */
  @Data
  public static final class PredicateConfig {
    private final String id;
    private final Map<String, String> params;
  }

  /**
   * Configuration for a behavior.
   */
  @Data
  public static final class BehaviorConfig {
    private final String id;
    private final Map<String, String> params;
  }
}
