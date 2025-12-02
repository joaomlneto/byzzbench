package byzzbench.simulator.faults.behaviors;

import byzzbench.simulator.domain.Action;
import byzzbench.simulator.domain.DropMessageAction;
import byzzbench.simulator.faults.FaultBehavior;
import byzzbench.simulator.faults.ScenarioContext;
import byzzbench.simulator.transport.Router;
import lombok.extern.java.Log;

import java.util.Collection;

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
    public Action toAction(ScenarioContext context) {
        return DropMessageAction.builder()
                .eventId(context.getEvent().orElseThrow().getEventId())
                .build();
    }

    @Deprecated
    public void accept(ScenarioContext context) {
        throw new UnsupportedOperationException("THIS SHOULD BE REMOVED - USE ACTIONS INSTEAD!");
    }
}
