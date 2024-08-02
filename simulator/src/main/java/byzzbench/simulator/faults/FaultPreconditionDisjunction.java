package byzzbench.simulator.faults;

import byzzbench.simulator.transport.MessageEvent;
import lombok.RequiredArgsConstructor;

import java.util.List;

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
