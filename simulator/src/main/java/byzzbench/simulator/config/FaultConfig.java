package byzzbench.simulator.config;


import java.io.Serializable;

/**
 * Configuration for a fault or mutation.
 * A fault is identified by a predicate and a behavior.
 */
public record FaultConfig(ByzzBenchConfig.PredicateConfig predicate,
                          FaultBehaviorConfig behavior) implements Serializable {
}
