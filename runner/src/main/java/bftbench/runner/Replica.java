package bftbench.runner;

import bftbench.runner.state.CommitLog;
import bftbench.runner.transport.MessagePayload;
import bftbench.runner.transport.Transport;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.java.Log;

import java.io.Serializable;
import java.security.MessageDigest;
import java.util.Set;

@Log
@Getter
public abstract class Replica<T extends Serializable> implements Serializable {
    static MessageDigest md;

    @Getter
    private final CommitLog<T> commitLog;

    @JsonIgnore
    private final transient String nodeId;

    @JsonIgnore
    private final transient Set<String> nodeIds;

    @JsonIgnore
    private final transient Transport transport;

    @SneakyThrows
    public Replica(String nodeId, Set<String> nodeIds, Transport transport, CommitLog commitLog) {
        this.nodeId = nodeId;
        this.nodeIds = nodeIds;
        this.transport = transport;
        this.commitLog = commitLog;
        md = MessageDigest.getInstance("SHA-1");
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

    public Serializable getState() {
        return this;
    }

    public void handleMessageWrapper(String sender, MessagePayload message) {
        try {
            log.info(String.format("Delivering %s message from %s to %s", message.getType(), sender, this.getNodeId()));
            this.handleMessage(sender, message);
        } catch (Exception e) {
            log.severe(String.format("Failed to handle message from %s: %s", sender, e.getMessage()));
            e.printStackTrace();
        }
    }

    public abstract void handleMessage(String sender, MessagePayload message) throws Exception;

    public void commitOperation(T message) {
        this.commitLog.add(message);
    }

    public void setTimeout(Runnable r, long timeout) {
        this.transport.setTimeout(this, r, timeout);
    }
}
