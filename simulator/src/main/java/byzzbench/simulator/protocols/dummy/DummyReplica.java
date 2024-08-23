package byzzbench.simulator.protocols.dummy;

import byzzbench.simulator.Replica;
import byzzbench.simulator.transport.MessagePayload;
import byzzbench.simulator.transport.Transport;
import java.io.Serializable;
import java.util.Set;
import lombok.ToString;
import lombok.extern.java.Log;

@Log
@ToString(callSuper = true)
public class DummyReplica extends Replica {

  public DummyReplica(String replicaId, Set<String> nodeIds,
                      Transport transport) {
    super(replicaId, null, null, null);
  }

  @Override
  public void initialize() {
    // nothing to do
  }

  @Override
  public void handleClientRequest(String clientId, Serializable request)
      throws Exception {
    // do nothing
  }

  @Override
  public void handleMessage(String sender, MessagePayload m) {
    // throw new RuntimeException("Unknown message type");
  }
}
