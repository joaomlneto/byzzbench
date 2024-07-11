package byzzbench.simulator.faults;

import byzzbench.simulator.transport.MessageEvent;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FaultPreconditionConjunction implements FaultPrecondition {
  private final List<FaultPrecondition> triggers;

  public FaultPreconditionConjunction(FaultPrecondition... triggers) {
    this.triggers = List.of(triggers);
  }

  @Override
  public boolean isSatisfiedBy(MessageEvent message) {
    return triggers.stream().allMatch(
        trigger -> trigger.isSatisfiedBy(message));
  }
}
