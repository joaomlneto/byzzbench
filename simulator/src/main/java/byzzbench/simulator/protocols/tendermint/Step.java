package byzzbench.simulator.protocols.tendermint;

public enum Step implements Comparable<Step> {
    PROPOSE,
    PREVOTE,
    PRECOMMIT,
    NONE
}
