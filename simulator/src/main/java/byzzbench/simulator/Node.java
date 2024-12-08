package byzzbench.simulator;

import byzzbench.simulator.transport.MessagePayload;

import java.io.Serializable;

/**
 * Represents a node in a distributed system, such as a client, a replica, or any other entity.
 */
public interface Node extends Serializable {
    /**
     * The unique ID of the node.
     */
    String getId();

    /**
     * Initialize the node. This method is called after the node is created, before any event is delivered.
     * Subclasses should override this method to perform any initialization that might be required.
     */
    void initialize();

    /**
     * Handle a message received by this node.
     *
     * @param sender  the ID of the sender
     * @param message the message payload
     * @throws Exception if an error occurs while handling the message
     */
    void handleMessage(String sender, MessagePayload message) throws Exception;
}
