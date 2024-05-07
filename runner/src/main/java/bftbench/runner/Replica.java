package bftbench.runner;

import bftbench.runner.transport.MessagePayload;
import bftbench.runner.transport.Transport;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.java.Log;

import java.io.Serializable;
import java.security.MessageDigest;
import java.util.Set;

@Log
@Getter
public abstract class Replica implements Serializable {
    private final transient MessageDigest md;

    @Getter(AccessLevel.NONE)
    private final CommitLog commitLog;

    @JsonIgnore
    private final transient String nodeId;

    @JsonIgnore
    private final transient Set<String> nodeIds;

    @JsonIgnore
    private final transient Transport transport;

    @SneakyThrows
    public Replica(String nodeId, Set<String> nodeIds, Transport transport) {
        this.nodeId = nodeId;
        this.nodeIds = nodeIds;
        this.transport = transport;
        this.commitLog = new CommitLog();
        this.md = MessageDigest.getInstance("SHA-1");
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

    protected byte[] digest(Serializable message) {
        return md.digest(message.toString().getBytes());
    }

    public Serializable getState() {
        return this;
    }

    public abstract void handleMessage(String sender, Serializable message) throws Exception;

    public void commitOperation(Serializable message) {
        this.commitLog.append(message);
    }
}
