package byzzbench.simulator.protocols.fasthotstuff;

import byzzbench.simulator.Replica;
import byzzbench.simulator.ScenarioExecutor;
import byzzbench.simulator.TerminationCondition;
import byzzbench.simulator.service.MessageMutatorService;
import byzzbench.simulator.service.SchedulesService;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.SortedSet;
import java.util.TreeSet;
import lombok.extern.java.Log;
import org.springframework.stereotype.Component;

@Component
@Log
public class FastHotStuffScenarioExecutor extends ScenarioExecutor {
  private final int NUM_NODES = 4;

  public FastHotStuffScenarioExecutor(
      MessageMutatorService messageMutatorService,
      SchedulesService schedulesService) {
    super("fasthotstuff", messageMutatorService, schedulesService);
  }

  @Override
  public void loadScenarioParameters(JsonNode parameters) {
    // no parameters to load
  }

  @Override
  public void setup() {
    try {
      SortedSet<String> nodeIds = new TreeSet<>();
      for (int i = 0; i < NUM_NODES; i++) {
        nodeIds.add(Character.toString((char)('A' + i)));
      }

      nodeIds.forEach(nodeId -> {
        Replica replica = new FastHotStuffReplica(nodeId, nodeIds, transport);
        this.addNode(replica);
      });
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public synchronized void run() {
    // nothing to do at the moment
    // TODO: genesis block creation logic should be moved here
  }

  @Override
  public TerminationCondition getTerminationCondition() {
    return new TerminationCondition() {
      @Override
      public boolean shouldTerminate() {
        return false;
      }
    };
  }
}
