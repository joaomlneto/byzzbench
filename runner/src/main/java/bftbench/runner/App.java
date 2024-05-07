package bftbench.runner;

import bftbench.runner.api.RestController;
import bftbench.runner.protocols.pbft.PbftScenarioExecutor;

public class App {
    public static void main(String[] args) {
        ScenarioExecutor scenarioExecutor = new PbftScenarioExecutor();
        RestController restController = new RestController(scenarioExecutor);
        restController.initialize();
        scenarioExecutor.run();
    }
}
