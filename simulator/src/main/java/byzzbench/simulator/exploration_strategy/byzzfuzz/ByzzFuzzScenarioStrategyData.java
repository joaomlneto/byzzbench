package byzzbench.simulator.exploration_strategy.byzzfuzz;

import byzzbench.simulator.exploration_strategy.ScenarioStrategyData;
import byzzbench.simulator.faults.Fault;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Map;

@Getter
@SuperBuilder
public class ByzzFuzzScenarioStrategyData extends ScenarioStrategyData {
    /**
     * The faults to be injected during the execution of the scenario
     */
    private final List<Fault> faults;

    /**
     * Information about replica progress
     */
    private final Map<String, ByzzFuzzRoundInfo> roundInfos;

    /**
     * The estimated round in which the replica is in
     */
    private final Map<String, Long> replicaRounds;

    /**
     * Maps the ID of a message to the round the replica was in
     */
    private final Map<Long, Long> messageRound;
}
