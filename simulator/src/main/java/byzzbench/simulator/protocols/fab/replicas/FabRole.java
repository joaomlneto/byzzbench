package byzzbench.simulator.protocols.fab.replicas;

/**
 * The role of a replica in the FAB protocol. A replica can act as more than one type of agents.
 * PROPOSER - proposing a value
 * ACCEPTOR - choosing a single correct value
 * LEARNERS - learning the chosen value
 */
public enum FabRole {
    ACCEPTOR,
    PROPOSER,
    LEARNER
}
