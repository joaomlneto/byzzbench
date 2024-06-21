package byzzbench.simulator.protocols.fasthotstuff.faults;

import byzzbench.simulator.faults.Fault;
import byzzbench.simulator.protocols.fasthotstuff.message.Block;
import byzzbench.simulator.transport.Event;
import byzzbench.simulator.transport.MessageEvent;

public class OmitMessage extends Fault {
    private final int round;
    private final String sender;
    private final Class<?> messageType;

    public OmitMessage(String sender, int round, Class<?> messageType) {
        super(null, null);
        this.sender = sender;
        this.round = round;
        this.messageType = messageType;
    }

    @Override
    public void apply(MessageEvent message) {
        System.out.println("OmitMessage.apply");
        System.out.println("message.getSenderId() = " + message.getSenderId() + " sender = " + sender);
        System.out.println("message.getClass() = " + message.getClass() + " messageType = " + messageType);
        System.out.println("message.getPayload() = " + message.getPayload() + " round = " + round);
        if (message.getSenderId().equals(sender)
                && message.getClass().equals(messageType)
                && message.getPayload() instanceof Block blockMessage &&
                blockMessage.getRound() == round) {
            System.out.println("DROPPING MESSAGE");
            message.setStatus(Event.Status.DROPPED);
        }
    }
}
