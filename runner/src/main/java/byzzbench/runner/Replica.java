package byzzbench.runner;

import byzzbench.runner.state.CommitLog;
import byzzbench.runner.transport.MessagePayload;
import byzzbench.runner.transport.Transport;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.java.Log;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Set;

@Log
@Getter
/*@Schema(type = "object",
        title = "Replica",
        description = "Replica"//,
        / *
        subTypes = {FastHotStuffReplica.class, PbftReplica.class, DummyReplica.class},
        discriminatorProperty = "type",
        discriminatorMapping = {
                @DiscriminatorMapping(value = "FastHotStuffReplica", schema = FastHotStuffReplica.class),
                @DiscriminatorMapping(value = "PbftReplica", schema = PbftReplica.class),
                @DiscriminatorMapping(value = "DummyReplica", schema = DummyReplica.class),
        }* /
)*/
@Serdeable
@ToString
public abstract class Replica<T extends Serializable> implements Serializable {
    static MessageDigest md;

    static {
        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @Getter
    private final String type = this.getClass().getSimpleName();

    @Getter
    private final transient CommitLog<T> commitLog;

    private final transient String nodeId;

    @JsonIgnore
    private final transient Set<String> nodeIds;

    @JsonIgnore
    private final transient Transport<T> transport;

    protected Replica(String nodeId, Set<String> nodeIds, Transport<T> transport, CommitLog<T> commitLog) {
        this.nodeId = nodeId;
        this.nodeIds = nodeIds;
        this.transport = transport;
        this.commitLog = commitLog;
    }

    protected void sendMessage(MessagePayload message, String recipient) {
        this.transport.sendMessage(this.nodeId, message, recipient);
    }

    protected void multicastMessage(MessagePayload message, Set<String> recipients) {
        this.transport.multicast(this.nodeId, recipients, message);
    }

    /**
     * Send message to all nodes in the system (except self)
     *
     * @param message the message to broadcast
     */
    protected void broadcastMessage(MessagePayload message) {
        Set<String> otherNodes = this.nodeIds
                .stream()
                .filter(nodeId -> !nodeId.equals(this.nodeId))
                .collect(java.util.stream.Collectors.toSet());
        this.transport.multicast(this.nodeId, otherNodes, message);
    }

    protected void broadcastMessageIncludingSelf(MessagePayload message) {
        this.transport.multicast(this.nodeId, this.nodeIds, message);
    }

    public byte[] digest(Serializable message) {
        return md.digest(message.toString().getBytes());
    }

    @JsonIgnore
    public Serializable getState() {
        return this;
    }

    public abstract void handleMessage(String sender, MessagePayload message) throws Exception;

    public void commitOperation(T message) {
        this.commitLog.add(message);
    }

    public void setTimeout(Runnable r, long timeout) {
        this.transport.setTimeout(this, r, timeout);
    }
}
