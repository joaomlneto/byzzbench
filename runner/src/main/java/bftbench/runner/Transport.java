package bftbench.runner;

import lombok.Data;
import lombok.Getter;
import lombok.extern.java.Log;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

@Log
public class Transport {
    private final AtomicLong messageSeqNum = new AtomicLong(1);
    private final Map<String, Node> nodes = new HashMap<>();

    @Getter
    private final Map<Long, CaptiveMessage> captiveMessages = new HashMap<>();

    @Data
    public static class CaptiveMessage {
        private final String senderId;
        private final String recipientId;
        private final Serializable message;
    }

    public void addNode(Node node) {
        nodes.put(node.getNodeId(), node);
    }

    public void removeNode(Node node) {
        nodes.remove(node.getNodeId());
    }

    public void sendMessage(String sender, Serializable message, String recipient) {
        this.multicast(sender, Set.of(recipient), message);
    }

    public void reset() {
        this.messageSeqNum.set(1);
        this.nodes.clear();
        this.captiveMessages.clear();
    }

    public void multicast(String sender, Set<String> recipients, Serializable message) {
        for (String recipient : recipients) {
            log.info("Queued: " + sender + "->" + recipient + ": " + message);
            try {
                captiveMessages.put(this.messageSeqNum.getAndIncrement(), new CaptiveMessage(sender, recipient, message));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
