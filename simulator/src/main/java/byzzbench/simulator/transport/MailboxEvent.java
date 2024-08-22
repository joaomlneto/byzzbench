package byzzbench.simulator.transport;

/**
 * Interface for events that can be sent to a mailbox.
 * This interface is used to ensure that all events have a recipient ID.
 */
public interface MailboxEvent {
    String getRecipientId();
}
