package byzzbench.simulator.scheduler;

import byzzbench.simulator.service.MessageMutatorService;
import byzzbench.simulator.transport.Event;
import byzzbench.simulator.transport.MessageEvent;
import byzzbench.simulator.transport.Transport;

import java.util.Comparator;
import java.util.Optional;

/**
 * A scheduler that delivers events in the order they were enqueued.
 */
public class FifoScheduler extends BaseScheduler {
  public FifoScheduler(MessageMutatorService messageMutatorService, Transport transport) { super("FIFO", messageMutatorService, transport); }

  @Override
  public Optional<EventDecision> scheduleNext() throws Exception {
    // Get the next event
    Optional<Event> event =
        getTransport()
            .getEventsInState(Event.Status.QUEUED)
            .stream()
            .filter(MessageEvent.class ::isInstance)
            .min(Comparator.comparingLong(Event::getEventId));

    if (event.isPresent()) {
      this.getTransport().deliverEvent(event.get().getEventId());
      EventDecision decision = new EventDecision(EventDecision.DecisionType.DELIVERED, event.get().getEventId());
      return Optional.of(decision);
    } else {
      return Optional.empty();
    }
  }

  @Override
  public void stopDropMessages() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'stopDropMessages'");
  }
}
