package byzzbench.simulator.exploration_strategy.byzzfuzz;

/**
 * A Scenario that contains the required elements to be used with the {@link ByzzFuzzExplorationStrategy}
 */
public interface ByzzFuzzScenario {
    /**
     * Get an oracle for extracting round information from messages and inferring the replicas' progress
     *
     * @return The round info oracle
     */
    ByzzFuzzRoundInfoOracle getRoundInfoOracle();
}
