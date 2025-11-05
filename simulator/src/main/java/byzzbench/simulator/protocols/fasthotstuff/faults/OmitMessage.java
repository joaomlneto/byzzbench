package byzzbench.simulator.protocols.fasthotstuff.faults;

import byzzbench.simulator.domain.FaultInjectionAction;
import byzzbench.simulator.faults.Fault;
import byzzbench.simulator.faults.ScenarioContext;
import byzzbench.simulator.protocols.fasthotstuff.message.Block;
import byzzbench.simulator.transport.Event;
import byzzbench.simulator.transport.MessageEvent;

import java.util.Optional;

public class OmitMessage extends Fault {
    private final int round;
    private final String sender;
    private final Class<?> messageType;

    public OmitMessage(String sender, int round, Class<?> messageType) {
        this.sender = sender;
        this.round = round;
        this.messageType = messageType;
    }

    @Override
    public boolean test(ScenarioContext state) {
        return false;
    }

    @Override
    public void accept(ScenarioContext ctx) {
        Optional<Event> e = ctx.getEvent();

        if (e.isEmpty()) {
            throw new IllegalArgumentException("Event is empty");
        }

        if (!(e.get() instanceof MessageEvent message)) {
            throw new IllegalArgumentException("Event is not a MessageEvent");
        }

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

    @Override
    public String getId() {
        return "OmitMessage";
    }

    @Override
    public String getName() {
        return "Omit Message";
    }

    @Override
    public FaultInjectionAction toAction(ScenarioContext context) {
        throw new UnsupportedOperationException("not yet implemented");
    }
}
