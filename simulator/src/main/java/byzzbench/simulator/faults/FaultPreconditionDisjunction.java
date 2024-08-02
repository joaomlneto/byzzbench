package byzzbench.simulator.faults;

import byzzbench.simulator.transport.MessageEvent;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FaultPreconditionDisjunction implements FaultPrecondition {
  private final List<FaultPrecondition> preconditions;

  public FaultPreconditionDisjunction(FaultPrecondition... preconditions) {
    this.preconditions = List.of(preconditions);
  }

  @Override
  public boolean isSatisfiedBy(MessageEvent message) {
    return preconditions.stream().anyMatch(
        precondition -> precondition.isSatisfiedBy(message));
  }
}
