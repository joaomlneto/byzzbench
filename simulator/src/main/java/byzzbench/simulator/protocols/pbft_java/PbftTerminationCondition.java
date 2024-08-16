package byzzbench.simulator.protocols.pbft_java;

import java.util.Random;

import byzzbench.simulator.TerminationCondition;

public class PbftTerminationCondition extends TerminationCondition {

    Random random = new Random();

    @Override
    public boolean shouldTerminate() {
        return random.nextInt(100) == 1;
    }

}
