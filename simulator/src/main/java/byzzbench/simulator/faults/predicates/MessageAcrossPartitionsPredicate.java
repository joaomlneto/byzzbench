package byzzbench.simulator.faults.predicates;

import byzzbench.simulator.faults.FaultPredicate;
import byzzbench.simulator.faults.ScenarioContext;
import byzzbench.simulator.transport.Event;
import byzzbench.simulator.transport.MessageEvent;
import byzzbench.simulator.transport.Router;

import java.util.Collection;

/**
 * Checks whether the message is crossing partitions
 */
public class MessageAcrossPartitionsPredicate implements FaultPredicate {
    /**
     * Router helper to determine if two nodes are connected
     */
    Router router = new Router();

    String name;

    /**
     * Create a new CreateNetworkPartitionsBehavior
     *
     * @param partitions A list of partitions to create
     */
    public MessageAcrossPartitionsPredicate(String[][] partitions) {
        for (String[] partition : partitions) {
            router.isolateNodes(partition);
        }
    }

    /**
     * Create a new CreateNetworkPartitionsBehavior
     *
     * @param partition A single partition to create
     */
    public MessageAcrossPartitionsPredicate(String[] partition) {
        this(new String[][]{partition});
    }

    /**
     * Create a new CreateNetworkPartitionsBehavior
     *
     * @param partition A single partition to create
     */
    public MessageAcrossPartitionsPredicate(Collection<String> partition) {
        this(partition.toArray(new String[0]));
    }

    /**
     * Create a new CreateNetworkPartitionsBehavior
     *
     * @param partition The ID of the node to isolate
     */
    public MessageAcrossPartitionsPredicate(String partition) {
        this(new String[]{partition});
    }

    @Override
    public String getId() {
        return "acrossnetworkpartitions-%s".formatted(this.router.getReversePartitionsMapping());
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
    public boolean test(ScenarioContext scenarioContext) {
        Event e = scenarioContext.getEvent().orElseThrow();

        if (!(e instanceof MessageEvent messageEvent)) {
            return false;
        }

        String sender = messageEvent.getSenderId();
        String recipient = messageEvent.getRecipientId();
        return !router.haveConnectivity(sender, recipient);
    }
}
