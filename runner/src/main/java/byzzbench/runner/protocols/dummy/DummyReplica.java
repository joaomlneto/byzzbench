package byzzbench.runner.protocols.dummy;

import byzzbench.runner.Replica;
import byzzbench.runner.transport.MessagePayload;
import byzzbench.runner.transport.Transport;
import io.micronaut.serde.annotation.Serdeable;
import lombok.ToString;
import lombok.extern.java.Log;

import java.io.Serializable;
import java.util.Set;

@Log
@Serdeable
@ToString(callSuper = true)
public class DummyReplica<O extends Serializable, R extends Serializable> extends Replica<O> {

    public DummyReplica(String replicaId,
                        Set<String> nodeIds,
                        Transport transport) {
        super(replicaId, null, null, null);
    }

    @Override
    public void handleMessage(String sender, MessagePayload m) {
        //throw new RuntimeException("Unknown message type");
    }

}
