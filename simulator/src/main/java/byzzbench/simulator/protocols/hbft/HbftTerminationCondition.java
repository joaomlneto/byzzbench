package byzzbench.simulator.protocols.hbft;

import java.util.Random;

import byzzbench.simulator.TerminationCondition;

public class HbftTerminationCondition extends TerminationCondition {

    Random random = new Random();

    @Override
    public boolean shouldTerminate() {
        return random.nextInt(100) == 1;
    }

}
