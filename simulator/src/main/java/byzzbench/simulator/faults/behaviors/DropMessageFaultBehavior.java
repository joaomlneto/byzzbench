package byzzbench.simulator.faults.behaviors;

import byzzbench.simulator.faults.FaultBehavior;
import byzzbench.simulator.transport.Event;
import byzzbench.simulator.transport.MessageEvent;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

@Component
public class DropMessageFaultBehavior implements FaultBehavior<MessageEvent> {
    @Override
    public void accept(MessageEvent message) {
        message.setStatus(Event.Status.DROPPED);
    }

    @Override
    public String getId() {
        return "Drop Message";
    }

    @Override
    public Collection<Class<? extends Event>> getInputClasses() {
        return List.of(MessageEvent.class);
    }
}
