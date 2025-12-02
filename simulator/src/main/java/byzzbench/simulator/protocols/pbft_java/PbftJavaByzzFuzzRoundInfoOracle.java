package byzzbench.simulator.protocols.pbft_java;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.exploration_strategy.byzzfuzz.ByzzFuzzRoundInfoOracle;
import byzzbench.simulator.exploration_strategy.byzzfuzz.MessageWithByzzFuzzRoundInfo;
import byzzbench.simulator.protocols.pbft_java.message.*;

public class PbftJavaByzzFuzzRoundInfoOracle extends ByzzFuzzRoundInfoOracle {
    public PbftJavaByzzFuzzRoundInfoOracle(Scenario scenario) {
        super(scenario);
    }

    @Override
    public int getProtocolMessageVerbIndex(MessageWithByzzFuzzRoundInfo message) {
        return switch (message) {
            case PrePrepareMessage ignored -> 1;
            case PrepareMessage ignored -> 2;
            case CommitMessage ignored -> 3;
            case ReplyMessage ignored -> 4;
            case ViewChangeMessage ignored -> 5;
            case NewViewMessage ignored -> 6;
            default -> throw new IllegalStateException("Unexpected value: " + message);
        };
    }

    @Override
    public int numRoundsToProcessRequest() {
        return 4; // pre-prepare, prepare, commit, reply
    }
}
