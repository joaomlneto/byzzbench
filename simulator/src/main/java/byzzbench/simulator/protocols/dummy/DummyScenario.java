package byzzbench.simulator.protocols.dummy;

import byzzbench.simulator.BaseScenario;
import byzzbench.simulator.Replica;
import byzzbench.simulator.TerminationCondition;
import byzzbench.simulator.scheduler.Scheduler;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.SortedSet;
import java.util.TreeSet;
import lombok.Getter;
import lombok.extern.java.Log;

@Getter
@Log
public class DummyScenario extends BaseScenario {
  private final TerminationCondition terminationCondition =
      new TerminationCondition() {
        @Override
        public boolean shouldTerminate() {
          return false;
        }
      };

  public DummyScenario(Scheduler scheduler) {
    super("dummy", scheduler);
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
        Replica replica = new DummyReplica(nodeId, nodeIds, transport);
        this.addNode(replica);
      });
      this.setNumClients(2);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public synchronized void run() {
    // nothing to do
  }
}
