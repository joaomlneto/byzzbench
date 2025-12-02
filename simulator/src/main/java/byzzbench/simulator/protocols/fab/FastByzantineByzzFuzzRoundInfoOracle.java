package byzzbench.simulator.protocols.fab;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.exploration_strategy.byzzfuzz.ByzzFuzzRoundInfoOracle;
import byzzbench.simulator.exploration_strategy.byzzfuzz.MessageWithByzzFuzzRoundInfo;
import byzzbench.simulator.protocols.fab.messages.*;

public class FastByzantineByzzFuzzRoundInfoOracle extends ByzzFuzzRoundInfoOracle {
    public FastByzantineByzzFuzzRoundInfoOracle(Scenario scenario) {
        super(scenario);
    }

    @Override
    public int getProtocolMessageVerbIndex(MessageWithByzzFuzzRoundInfo message) {
        return switch (message) {
            case ProposeMessage ignored -> 1;
            case AcceptMessage ignored -> 2;
            case LearnMessage ignored -> 3;
            case ReplyMessage ignored -> 4;
            case ViewChangeMessage ignored -> 5;
            case NewViewMessage ignored -> 6;
            default -> throw new IllegalStateException("Unexpected value: " + message);
        };
    }

    @Override
    public int numRoundsToProcessRequest() {
        return 4; // propose, accept, learn, reply
    }
}
