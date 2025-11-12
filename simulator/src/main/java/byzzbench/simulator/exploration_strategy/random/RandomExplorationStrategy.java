package byzzbench.simulator.exploration_strategy.random;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.domain.Action;
import byzzbench.simulator.domain.DeliverMessageAction;
import byzzbench.simulator.exploration_strategy.ExplorationStrategy;
import byzzbench.simulator.exploration_strategy.ExplorationStrategyParameters;
import lombok.extern.java.Log;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * A exploration_strategy that randomly selects events to deliver, drop, mutate or timeout.
 */
@Component
@Log
public class RandomExplorationStrategy extends ExplorationStrategy {
    public <T> T getRandomElement(List<T> list) {
        return list.get(this.getRand().nextInt(list.size()));
    }

    @Override
    public void initializeScenario(Scenario scenario) {
        // no initialization needed
    }

    @Override
    public synchronized Optional<Action> scheduleNext(Scenario scenario) {
        List<Action> actions = this.getAvailableActions(scenario);

        // pick a random action
        if (actions.isEmpty()) {
            log.warning("No available actions!");
            return Optional.empty();
        }

        Action action = getRandomElement(actions);
        action.accept(scenario);

        // update metadata
        switch (action) {
            case DeliverMessageAction ignored -> {
                this.remainingDropMessages.compute(scenario, (k, v) -> v != null && v > 0 ? v - 1 : 0);
            }
            default -> {
            }
        }

        return Optional.of(action);

        /*

        // Get a random event
        List<Event> availableEvents = scenario.getTransport().getEventsInState(Event.Status.QUEUED);

        // if there are no events, return empty
        if (availableEvents.isEmpty()) {
            log.warning("No queued events (messages, timeouts)!");
            return Optional.empty();
        }

        SortedSet<String> faultyReplicaIds = scenario.getFaultyReplicaIds();
        List<TimeoutEvent> timeoutEvents = this.getQueuedTimeoutEvents(scenario);
        List<MessageEvent> messageEvents = availableEvents.stream().filter(MessageEvent.class::isInstance).map(MessageEvent.class::cast).toList();
        List<MessageEvent> mutateableMessageEvents = messageEvents.stream().filter(msg -> faultyReplicaIds.contains(msg.getSenderId())).toList();

        int timeoutWeight = timeoutEvents.size() * this.getDeliverTimeoutWeight();
        int deliverMessageWeight = messageEvents.size() * this.getDeliverMessageWeight();
        int dropMessageWeight = (messageEvents.size() * this.dropMessageWeight(scenario));
        int mutateMessageWeight = (mutateableMessageEvents.size() * this.mutateMessageWeight(scenario));
        int dieRoll = this.getRand().nextInt(timeoutWeight + deliverMessageWeight + dropMessageWeight + mutateMessageWeight);

        // check if we should trigger a timeout
        dieRoll -= timeoutWeight;
        if (dieRoll < 0) {
            Event timeout = getRandomElement(timeoutEvents);
            scenario.getTransport().deliverEvent(timeout.getEventId());
            Action decision = TriggerTimeoutAction.builder().timeoutEventId(timeout.getEventId()).build();
            return Optional.of(decision);
        }

        // check if we should deliver a message (without changes)
        dieRoll -= deliverMessageWeight;
        if (dieRoll < 0) {
            Event message = getNextMessageEvent(scenario, messageEvents);
            scenario.getTransport().deliverEvent(message.getEventId());
            Action decision = DeliverMessageAction.builder().messageEventId(message.getEventId()).build();
            return Optional.of(decision);
        }

        // check if we should drop a message sent between nodes
        dieRoll -= dropMessageWeight;
        if (dieRoll < 0) {
            Event message = getRandomElement(messageEvents);
            scenario.getTransport().dropEvent(message.getEventId());
            //Action decision = FaultInjectionAction.builder().faultBehaviorId("drop-message").eventId(message.getEventId()).build();
            //return Optional.of(decision);
            throw new UnsupportedOperationException("not implemented yet!!!");
        }

        // check if we should mutate-and-deliver a message sent between nodes
        dieRoll -= mutateMessageWeight;
        if (dieRoll < 0) {
            Event message = getRandomElement(mutateableMessageEvents);
            List<MessageMutationFault> mutators = ApplicationContextProvider.getMessageMutatorService().getMutatorsForEvent(message);

            if (mutators.isEmpty()) {
                // no mutators, return nothing
                log.warning("No mutators available for message " + message.getEventId());
                return Optional.empty();
            }
            scenario.getTransport().applyMutation(
                    message.getEventId(),
                    getRandomElement(mutators));
            scenario.getTransport().deliverEvent(message.getEventId());

            throw new UnsupportedOperationException("not implemented yet!!!");
            //Action decision = FaultInjectionAction.builder().faultBehaviorId("mutate-and-deliver").eventId(message.getEventId()).build();
            //return Optional.of(decision);
        }*/

        //throw new IllegalStateException("This should never happen!");
    }

    @Override
    public void reset() {
        // nothing to do
    }

    @Override
    public void loadSchedulerParameters(ExplorationStrategyParameters parameters) {
        // no parameters to load
    }
}
