package byzzbench.simulator.protocols.hbft;

import byzzbench.simulator.BaseScenario;
import byzzbench.simulator.Replica;
import byzzbench.simulator.Scenario;
import byzzbench.simulator.ScenarioPredicate;
import byzzbench.simulator.scheduler.Scheduler;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.SortedSet;
import java.util.TreeSet;
import lombok.Getter;
import lombok.extern.java.Log;

@Getter
@Log
public class HbftScenario extends BaseScenario {
  private final ScenarioPredicate terminationCondition =
      new ScenarioPredicate() {
        @Override
        public boolean test(Scenario context) {
          return false;
        }
      };

  public HbftScenario(Scheduler scheduler) {
    super("hbft", scheduler);
    setNumClients(2);
  }

  @Override
  public void loadScenarioParameters(JsonNode parameters) {
    // no parameters to load
  }

  @Override
  protected void setup() {
    try {
      SortedSet<String> nodeIds = new TreeSet<>();
      for (int i = 0; i < 2; i++) {
        nodeIds.add(Character.toString((char)('A' + i)));
      }
      nodeIds.forEach(nodeId -> {
        Replica replica =
            new HbftReplica(nodeId, nodeIds, transport, timekeeper);
        this.addNode(replica);
      });
      this.setNumClients(1);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public synchronized void run() {
    // nothing to do
  }
}
