package byzzbench.simulator.exploration_strategy.byzzfuzz;

import byzzbench.simulator.exploration_strategy.ScenarioStrategyData;
import byzzbench.simulator.faults.Fault;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@SuperBuilder
public class ByzzFuzzScenarioStrategyData extends ScenarioStrategyData {
    private final int numRoundsWithProcessFaults;
    private final int numRoundsWithNetworkFaults;
    private final int numRoundsWithFaults;
    private final List<Fault> faults;
}
