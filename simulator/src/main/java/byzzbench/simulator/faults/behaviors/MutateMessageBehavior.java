package byzzbench.simulator.faults.behaviors;

import byzzbench.simulator.ApplicationContextUtils;
import byzzbench.simulator.faults.FaultBehavior;
import byzzbench.simulator.faults.ScenarioContext;
import byzzbench.simulator.faults.faults.MessageMutationFault;
import byzzbench.simulator.service.MessageMutatorService;
import byzzbench.simulator.transport.Event;
import byzzbench.simulator.transport.MessageEvent;
import byzzbench.simulator.transport.MessagePayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * Inject a random message mutation into the message.
 */
@RequiredArgsConstructor
@Log
@Component
public class MutateMessageBehavior implements FaultBehavior {
    private static final Random rand = new Random();

    @Override
    public String getId() {
        return "mutatemessage";
    }

    @Override
    public String getName() {
        return "Apply random mutation";
    }

    @Override
    public void accept(ScenarioContext context) {
        Optional<Event> event = context.getEvent();

        if (event.isEmpty()) {
            log.warning("No event to mutate");
            return;
        }

        Event e = event.get();

        if (!(e instanceof MessageEvent messageEvent)) {
            log.warning("Event is not a message event");
            return;
        }

        MessagePayload payload = messageEvent.getPayload();

        // get available mutators for message
        ApplicationContext ctx = ApplicationContextUtils.getApplicationContext();
        MessageMutatorService messageMutatorService = ctx.getBean(MessageMutatorService.class);
        List<MessageMutationFault> mutators = messageMutatorService.getMutatorsForClass(payload.getClass());

        if (mutators.isEmpty()) {
            log.warning("No mutators available for message!");
            return;
        }

        // apply the random mutator
        MessageMutationFault mutator = mutators.get(rand.nextInt(mutators.size()));

        // apply the mutation if the message is queued
        if (e.getStatus() == Event.Status.QUEUED) {
            context.getScenario().getTransport().applyMutation(e.getEventId(), mutator);
        }
    }
}
