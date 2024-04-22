package bftbench.runner;

import bftbench.runner.transport.Transport;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.extern.java.Log;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Set;

@Log
@Getter
public abstract class Node implements Serializable {
    private final transient MessageDigest md;
    @JsonIgnore
    private final transient String nodeId;
    @JsonIgnore
    private final transient Set<String> nodeIds;
    @JsonIgnore
    private final transient Transport transport;

    public Node(String nodeId, Set<String> nodeIds, Transport transport) throws NoSuchAlgorithmException {
        this.nodeId = nodeId;
        this.nodeIds = nodeIds;
        this.transport = transport;
        this.md = MessageDigest.getInstance("SHA-1");
    }

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



    protected byte[] digest(Serializable message) {
        return md.digest(message.toString().getBytes());
    }

    public abstract void handleMessage(String sender, Serializable message) throws Exception;
}
