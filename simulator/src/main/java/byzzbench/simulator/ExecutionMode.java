package byzzbench.simulator;

/**
 * Controls which actions are exposed by default to the exploration strategy.
 */
public enum ExecutionMode {
    /**
     * The exploration strategy can deliver any message that is currently queued.
     * This is the default behavior.
     */
    ASYNC,
    /**
     * The exploration strategy will deliver the earliest-sent message that is currently queued.
     * This follows the communication-closure hypothesis.
     * This should be used with a non-zero "dropMessageWeight" to emulate the behavior in ByzzFuzz.
     * This also disables timeout delivery if there are any queued messages.
     */
    SYNC,
}
