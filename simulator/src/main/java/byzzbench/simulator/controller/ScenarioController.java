package byzzbench.simulator.controller;

import byzzbench.simulator.service.ScenarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ScenarioController {
    private final ScenarioService scenarioService;

    // get scenario
    @GetMapping("/scenario/{scenarioId}")
    public void getScenario(@PathVariable String scenarioId) {
        System.out.println("getScenario: " + scenarioId);
        throw new UnsupportedOperationException("Not implemented");
    }
}
