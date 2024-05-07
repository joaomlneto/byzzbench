package bftbench.runner.protocols.pbft.pojo;

import java.io.Serializable;

public enum ReplicaTicketPhase implements Serializable {
    /**
     * Represents the initial phase of the ticket.
     */
    PRE_PREPARE,
    /**
     * Represents the phase of the ticket after it the
     * {@code prepared} condition becomes {@code true}
     */
    PREPARE,
    /**
     * Represents the phase of the ticket after it the
     * {@code committed} condition becomes {@code true}
     */
    COMMIT
}
