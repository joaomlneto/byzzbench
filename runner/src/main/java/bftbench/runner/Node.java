package bftbench.runner;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

import java.io.Serializable;
import java.util.Set;

@Log
@RequiredArgsConstructor
@Getter
public abstract class Node implements Serializable {
    private final String nodeId;
    private final transient Set<String> nodeIds;
    private final transient Transport transport;

    protected void sendMessage(Serializable message, String recipient) {
        this.transport.sendMessage(this.nodeId, message, recipient);
    }

    protected void multicastMessage(Serializable message, Set<String> recipients) {
        this.transport.multicast(this.nodeId, recipients, message);
    }

    /**
     * Send message to all nodes in the system (except self)
     * @param message the message to broadcast
     */
    protected void broadcastMessage(Serializable message) {
        Set<String> otherNodes = this.nodeIds
                .stream()
                .filter(nodeId -> !nodeId.equals(this.nodeId))
                .collect(java.util.stream.Collectors.toSet());
        this.transport.multicast(this.nodeId, otherNodes, message);
    }

    public abstract void handleMessage(String sender, Serializable message) throws Exception;
}
