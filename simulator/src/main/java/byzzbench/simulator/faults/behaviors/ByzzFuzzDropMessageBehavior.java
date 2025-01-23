package byzzbench.simulator.faults.behaviors;

import byzzbench.simulator.faults.FaultBehavior;
import byzzbench.simulator.faults.FaultContext;
import byzzbench.simulator.transport.Event;
import byzzbench.simulator.transport.MessageEvent;
import byzzbench.simulator.transport.Router;
import lombok.extern.java.Log;

import java.util.Collection;
import java.util.Optional;

@Log
public class ByzzFuzzDropMessageBehavior implements FaultBehavior {
    /**
     * Router helper to determine if two nodes are connected
     */
    Router router = new Router();

    /**
     * Memoized name of the fault
     */
    String name;

    /**
     * Create a new CreateNetworkPartitionsBehavior
     *
     * @param partitions A list of partitions to create
     */
    public ByzzFuzzDropMessageBehavior(String[][] partitions) {
        for (String[] partition : partitions) {
            router.isolateNodes(partition);
        }
    }

    /**
     * Create a new CreateNetworkPartitionsBehavior
     *
     * @param partition A single partition to create
     */
    public ByzzFuzzDropMessageBehavior(String[] partition) {
        this(new String[][]{partition});
    }

    /**
     * Create a new CreateNetworkPartitionsBehavior
     *
     * @param partition A single partition to create
     */
    public ByzzFuzzDropMessageBehavior(Collection<String> partition) {
        this(partition.toArray(new String[0]));
    }

    /**
     * Create a new CreateNetworkPartitionsBehavior
     *
     * @param partition The ID of the node to isolate
     */
    public ByzzFuzzDropMessageBehavior(String partition) {
        this(new String[]{partition});
    }

    @Override
    public String getId() {
        return "createnetworkpartitions-%s".formatted(this.router.getReversePartitionsMapping());
    }

    @Override
    public String getName() {
        if (this.name == null) {
            // convert the array of arrays into a string
            String partitions = this.router.getReversePartitionsMapping().stream()
                    .map(p -> "[" + String.join(",", p) + "]")
                    .reduce("", (a, b) -> a + b);
            this.name = "Drop message based on partitions %s".formatted(partitions);
        }
        return this.name;
    }

    @Override
    public void accept(FaultContext context) {
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

        String sender = messageEvent.getSenderId();
        String recipient = messageEvent.getRecipientId();

        // if the sender and recipient are in the same partition, do nothing
        if (router.haveConnectivity(sender, recipient)) {
            return;
        }

        // otherwise, drop the message: the sender and recipient are in different partitions
        context.getScenario().getTransport().dropEvent(e.getEventId());
    }
}
