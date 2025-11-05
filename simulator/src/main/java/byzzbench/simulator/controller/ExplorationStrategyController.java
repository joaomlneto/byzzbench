package byzzbench.simulator.controller;


import byzzbench.simulator.Scenario;
import byzzbench.simulator.domain.Action;
import byzzbench.simulator.exploration_strategy.ExplorationStrategy;
import byzzbench.simulator.exploration_strategy.ScenarioStrategyData;
import byzzbench.simulator.service.ExplorationStrategyService;
import byzzbench.simulator.service.ScenarioService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

/**
 * REST controller for schedulers
 */
@RestController
@RequiredArgsConstructor
public class ExplorationStrategyController {
    private final ExplorationStrategyService explorationStrategyService;
    private final ScenarioService scenarioService;


    /**
     * Get the list of schedulers available in the simulator.
     *
     * @return The list of exploration_strategy IDs.
     */
    @GetMapping("/schedulers")
    public List<String> getSchedulers() {
        return explorationStrategyService.getSchedulerIds();
    }

    /**
     * Get exploration strategy metadata on a given scenario
     *
     * @param schedulerId The ID of the exploration strategy.
     * @param scenarioId  The ID of the scenario to use.
     * @return The action that was executed.
     */
    @GetMapping("/schedulers/{schedulerId}/scenario/{scenarioId}")
    public ScenarioStrategyData getScenarioStrategyData(@NonNull @PathVariable("schedulerId") String schedulerId,
                                                        @PathVariable("scenarioId") long scenarioId) {
        ExplorationStrategy explorationStrategy = explorationStrategyService.getExplorationStrategy(schedulerId);
        Scenario scenario = scenarioService.getScenarioById(scenarioId);
        return explorationStrategy.getScenarioStrategyData(scenario);
    }

    /**
     * Get the set of available actions in the scenario
     *
     * @param schedulerId The ID of the exploration strategy to use.
     * @param scenarioId  The ID of the scenario to use.
     * @return The action that was executed.
     */
    @GetMapping("/schedulers/{schedulerId}/scenario/{scenarioId}/actions")
    public List<Action> getStrategyAvailableActions(@NonNull @PathVariable("schedulerId") String schedulerId,
                                                    @PathVariable("scenarioId") long scenarioId) {
        ExplorationStrategy explorationStrategy = explorationStrategyService.getExplorationStrategy(schedulerId);
        Scenario scenario = scenarioService.getScenarioById(scenarioId);
        return explorationStrategy.getAvailableActions(scenario);
    }

    /**
     * Execute an action in the scenario
     *
     * @param schedulerId The ID of the exploration strategy to use.
     * @param scenarioId  The ID of the scenario to use.
     * @return The action that was executed.
     */
    @PostMapping("/schedulers/{schedulerId}/scenario/{scenarioId}/execute")
    public Optional<Action> executeSchedulerAction(@NonNull @PathVariable("schedulerId") String schedulerId,
                                                   @PathVariable("scenarioId") long scenarioId) {
        ExplorationStrategy explorationStrategy = explorationStrategyService.getExplorationStrategy(schedulerId);
        Scenario scenario = scenarioService.getScenarioById(scenarioId);
        return explorationStrategy.scheduleNext(scenario);
    }

    /**
     * Execute a specific action in the scenario
     *
     * @param schedulerId The ID of the exploration strategy to use.
     * @param scenarioId  The ID of the scenario to use.
     * @param actionId    The ID (index) of the action to use.
     * @return The action that was executed.
     */
    @PostMapping("/schedulers/{schedulerId}/scenario/{scenarioId}/execute/{actionId}")
    public Action executeSpecificSchedulerAction(@NonNull @PathVariable("schedulerId") String schedulerId,
                                                 @NonNull @PathVariable("scenarioId") Long scenarioId,
                                                 @NonNull @PathVariable("actionId") Integer actionId) {
        ExplorationStrategy explorationStrategy = explorationStrategyService.getExplorationStrategy(schedulerId);
        Scenario scenario = scenarioService.getScenarioById(scenarioId);
        List<Action> actions = explorationStrategy.getAvailableActions(scenario);
        Action action = Optional.of(actions.get(actionId)).orElseThrow();
        System.out.println("ACTION TO BE EXECUTED: " + action);
        action.accept(scenario);
        return action;
    }

    /**
     * Get the currently selected exploration strategy
     *
     * @return Data about
     */
    @GetMapping("/schedulers/{schedulerId}")
    public ExplorationStrategy getScheduler(@NonNull @PathVariable("schedulerId") String schedulerId) {
        return explorationStrategyService.getExplorationStrategy(schedulerId);
    }
}
