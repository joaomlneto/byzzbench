package byzzbench.simulator.controller;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.domain.Schedule;
import byzzbench.simulator.service.ScenarioService;
import byzzbench.simulator.service.SimulatorService;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ScenarioController {
    private final ScenarioService scenarioService;
    private final SimulatorService simulatorService;

    /**
     * Get the list of all currently-materialized scenarios
     *
     * @return a list of scenario names
     */
    @GetMapping("/scenarios")
    public List<String> getScenarios() {
        return scenarioService.getScenarioIds();
    }

    /**
     * Get the specified scenario.
     *
     * @param scenarioId the ID of the scenario to retrieve
     */
    @GetMapping("/scenarios/{scenarioId}")
    public Scenario getScenario(@PathVariable String scenarioId) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Get the schedule of the specified scenario.
     *
     * @param scenarioId the ID of the scenario whose schedule to retrieve
     */
    @GetMapping("/scenarios/{scenarioId}/schedule")
    public Schedule getScenarioSchedule(@PathVariable int scenarioId) {
        return scenarioService.getScenarios().get(scenarioId).getSchedule();
    }

    /**
     * Generate a new scenario.
     */
    @PostMapping("/scenarios")
    public String createScenario() {
        String scenarioId = simulatorService.getScenario().getId();
        this.simulatorService.changeScenario(scenarioId, JsonNodeFactory.instance.nullNode());
        return simulatorService.getScenario().getId();
    }
}
