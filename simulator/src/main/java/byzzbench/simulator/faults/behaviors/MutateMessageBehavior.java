package byzzbench.simulator.faults.behaviors;

import byzzbench.simulator.domain.FaultInjectionAction;
import byzzbench.simulator.faults.FaultBehavior;
import byzzbench.simulator.faults.ScenarioContext;
import byzzbench.simulator.faults.faults.MessageMutationFault;
import byzzbench.simulator.service.ApplicationContextProvider;
import byzzbench.simulator.service.MessageMutatorService;
import byzzbench.simulator.transport.Event;
import byzzbench.simulator.transport.MessageEvent;
import byzzbench.simulator.transport.MessagePayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
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
    @Override
    public String getId() {
        return "mutatemessage";
    }

    @Override
    public String getName() {
        return "Apply random mutation";
    }

    @Override
    public FaultInjectionAction toAction(ScenarioContext context) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Deprecated
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
        MessageMutatorService messageMutatorService = ApplicationContextProvider.getMessageMutatorService();
        List<MessageMutationFault> mutators = messageMutatorService.getMutatorsForClass(payload.getClass());

        if (mutators.isEmpty()) {
            log.warning("No mutators available for message!");
            return;
        }

        // apply the random mutator - use the scenario Random number generator!!
        Random rand = context.getScenario().getRandom();
        MessageMutationFault mutator = mutators.get(rand.nextInt(mutators.size()));

        // apply the mutation if the message is queued
        if (e.getStatus() == Event.Status.QUEUED) {
            context.getScenario().getTransport().applyMutation(e.getEventId(), mutator);
        }
    }
}
