package bftbench.runner;

import bftbench.runner.pbft.MessageLog;
import bftbench.runner.pbft.PbftNode;
import bftbench.runner.pbft.message.RequestMessage;
import lombok.extern.java.Log;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

@Log
public class PbftRunner {
    private final int numNodes;

    public PbftRunner(int numNodes) {
        this.numNodes = numNodes;
    }

    private final Transport transport = new Transport();
    private final Map<String, Node> nodes = new HashMap<>();

    public synchronized void run() {
        try {
            Set<String> nodeIds = new TreeSet<>();
            for (int i = 0; i < numNodes; i++) {
                nodeIds.add("p" + i);
            }

            for (int i = 0; i < numNodes; i++) {
                String nodeId = "p" + i;
                MessageLog messageLog = new MessageLog(100, 100, 200);
                nodes.put(nodeId, new PbftNode(nodeId, nodeIds, 1, 1000, messageLog, transport));
            }

            for (Node node : nodes.values()) {
                transport.addNode(node);
            }

            try {
                // get current timestamp
                RequestMessage m = new RequestMessage("123", System.currentTimeMillis(), "c0");
                nodes.get("p1").handleMessage("c0", m);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized void reset() {
        this.transport.reset();
        this.nodes.clear();
        this.run();
    }

    public synchronized Map<String, Node> nodes() {
        return this.nodes;
    }

    public synchronized Map<Long, Transport.CaptiveMessage> captiveMessages() {
        return this.transport.getCaptiveMessages();
    }

    public synchronized Serializable deliverMessage(long messageId) throws Exception {
        // remove from captive messages
        Transport.CaptiveMessage message = this.transport.getCaptiveMessages().remove(messageId);

        this.nodes.get(message.getRecipientId()).handleMessage(message.getSenderId(), message.getMessage());

        // deliver message
        return message.getMessage();
    }
}
