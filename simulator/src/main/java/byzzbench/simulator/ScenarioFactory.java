package byzzbench.simulator;

import byzzbench.simulator.service.MessageMutatorService;
import com.fasterxml.jackson.databind.JsonNode;

public interface ScenarioFactory {
  default String getId() { return this.getClass().getSimpleName(); }
  Scenario createScenario(MessageMutatorService messageMutatorService,
                          JsonNode params);
}
