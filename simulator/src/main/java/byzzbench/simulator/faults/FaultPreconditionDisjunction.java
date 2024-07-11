package byzzbench.simulator.faults;

import byzzbench.simulator.transport.MessageEvent;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FaultPreconditionDisjunction implements FaultPrecondition {
  private final List<FaultPrecondition> triggers;

  public FaultPreconditionDisjunction(FaultPrecondition... triggers) {
    this.triggers = List.of(triggers);
  }

  @Override
  public boolean isSatisfiedBy(MessageEvent message) {
    return triggers.stream().anyMatch(
        trigger -> trigger.isSatisfiedBy(message));
  }
}
