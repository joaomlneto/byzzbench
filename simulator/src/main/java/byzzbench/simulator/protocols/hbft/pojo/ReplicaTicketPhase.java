package byzzbench.simulator.protocols.hbft.pojo;

public enum ReplicaTicketPhase {
    /**
     * Represents the initial phase of the ticket.
     */
    PREPARE,
    /**
     * Represents the phase of the ticket after it the
     * {@code committed} condition becomes {@code true}
     */
    COMMIT
}
