package bftbench.runner;

import bftbench.runner.api.RestController;
import bftbench.runner.protocols.fasthotstuff.FastHotStuffScenarioExecutor;

public class App {
    public static void main(String[] args) {
        //ScenarioExecutor scenarioExecutor = new PbftScenarioExecutor();
        ScenarioExecutor scenarioExecutor = new FastHotStuffScenarioExecutor();
        RestController restController = new RestController(scenarioExecutor);
        restController.initialize();
        scenarioExecutor.run();
    }
}
