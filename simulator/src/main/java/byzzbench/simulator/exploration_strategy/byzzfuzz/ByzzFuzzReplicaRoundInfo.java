package byzzbench.simulator.exploration_strategy.byzzfuzz;

/**
 * Class representing the estimated round of a given replica during protocol execution.
 */
public class ByzzFuzzReplicaRoundInfo {
    /**
     * The latest ByzzFuzz round information associated with the replica.
     */
    private final ByzzFuzzRoundInfo byzzFuzzRoundInfo = new ByzzFuzzRoundInfo(0, 0, 0);
    /**
     * The identifier of the replica.
     */
    private String replicaId;
}
