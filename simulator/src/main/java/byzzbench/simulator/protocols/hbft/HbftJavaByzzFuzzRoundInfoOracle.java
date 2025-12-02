package byzzbench.simulator.protocols.hbft;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.exploration_strategy.byzzfuzz.ByzzFuzzRoundInfoOracle;
import byzzbench.simulator.exploration_strategy.byzzfuzz.MessageWithByzzFuzzRoundInfo;
import byzzbench.simulator.protocols.hbft.message.*;

public class HbftJavaByzzFuzzRoundInfoOracle extends ByzzFuzzRoundInfoOracle {
    public HbftJavaByzzFuzzRoundInfoOracle(Scenario scenario) {
        super(scenario);
    }

    @Override
    public int getProtocolMessageVerbIndex(MessageWithByzzFuzzRoundInfo message) {
        return switch (message) {
            case PrepareMessage ignored -> 1;
            case CommitMessage ignored -> 2;
            case ReplyMessage ignored -> 3;
            case CheckpointMessage ignored -> 4;
            case ViewChangeMessage ignored -> 5;
            case NewViewMessage ignored -> 6;
            default -> throw new IllegalStateException("Unexpected value: " + message);
        };
    }

    @Override
    public int numRoundsToProcessRequest() {
        return 3; // prepare, commit, reply
    }
}
