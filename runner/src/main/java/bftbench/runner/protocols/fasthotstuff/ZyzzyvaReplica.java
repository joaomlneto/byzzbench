package bftbench.runner.protocols.fasthotstuff;

import bftbench.runner.Replica;
import bftbench.runner.transport.Transport;
import lombok.Getter;
import lombok.extern.java.Log;

import java.io.Serializable;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

@Log
public class ZyzzyvaReplica extends Replica {
    @Getter
    private final AtomicLong seqCounter = new AtomicLong(1);

    public ZyzzyvaReplica(String nodeId, Set<String> nodeIds, Transport transport) {
        super(nodeId, nodeIds, transport);
    }

    @Override
    public void handleMessage(String sender, Serializable message) throws Exception {
        log.info(String.format("Received message from %s: %s", sender, message));


    }
}
