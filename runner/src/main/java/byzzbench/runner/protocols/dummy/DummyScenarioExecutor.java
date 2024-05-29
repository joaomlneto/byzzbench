package byzzbench.runner.protocols.dummy;

import byzzbench.runner.Replica;
import byzzbench.runner.ScenarioExecutor;
import byzzbench.runner.protocols.pbft.MessageLog;
import byzzbench.runner.protocols.pbft.message.RequestMessage;
import byzzbench.runner.protocols.pbft.mutator.PrePrepareMessageMutatorFactory;
import byzzbench.runner.transport.Transport;
import lombok.extern.java.Log;

import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;

@Log
public class DummyScenarioExecutor<T extends Serializable> extends ScenarioExecutor<T> {

    public DummyScenarioExecutor() {
        super(new Transport());
    }

    @Override
    public void setup() {
        try {
            Set<String> nodeIds = new TreeSet<>();
            for (int i = 0; i < 4; i++) {
                nodeIds.add(Character.toString((char) ('A' + i)));
            }

            nodeIds.forEach(nodeId -> {
                MessageLog messageLog = new MessageLog(100, 100, 200);
                Replica replica = new DummyReplica<String, String>(nodeId, nodeIds, transport);
                nodes.put(nodeId, replica);
                transport.addNode(replica);
            });

            transport.registerMessageMutators(new PrePrepareMessageMutatorFactory());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized void run() {
        try {
            RequestMessage m = new RequestMessage("123", System.currentTimeMillis(), "c0");
            nodes.get("A").handleMessage("c0", m);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
