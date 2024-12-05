package byzzbench.simulator.protocols.basic_hotstuff.messages;

public enum MessageType {
    NEW_VIEW,
    PREPARE,
    PREPARE_VOTE,
    PRE_COMMIT,
    PRE_COMMIT_VOTE,
    COMMIT,
    COMMIT_VOTE,
    DECIDE
}
