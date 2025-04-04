package byzzbench.simulator;

import byzzbench.simulator.transport.MessagePayload;
import byzzbench.simulator.transport.Transport;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.Instant;

/**
 * Represents a node in a distributed system, such as a client, a replica, or any other entity.
 */
@SuperBuilder
@NoArgsConstructor
public abstract class Node implements Serializable {
    /**
     * The unique ID of the node.
     */
    public abstract String getId();

    /**
     * Initialize the node. This method is called after the node is created, before any event is delivered.
     * Subclasses should override this method to perform any initialization that might be required.
     */
    public abstract void initialize();

    /**
     * Get the transport object used by this node.
     *
     * @return the transport object used by this node
     */
    public abstract Transport getTransport();

    /**
     * Handle a message received by this node.
     *
     * @param sender  the ID of the sender
     * @param message the message payload
     */
    public abstract void handleMessage(String sender, MessagePayload message);

    /**
     * Get the current time from the timekeeper.
     */
    public abstract Instant getCurrentTime();

    /**
     * Send a message to another node in the system.
     *
     * @param message   the message to send
     * @param recipient the recipient of the message
     */
    public void sendMessage(MessagePayload message, String recipient) {
        message.sign(this.getId());
        this.getTransport().sendMessage(this, message, recipient);
    }

}
