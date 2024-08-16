package byzzbench.simulator.protocols.pbft_java;

import byzzbench.simulator.TerminationCondition;
import java.util.Random;

public class PbftTerminationCondition extends TerminationCondition {

  Random random = new Random();

  @Override
  public boolean shouldTerminate() {
    return random.nextInt(100) == 1;
  }
}
