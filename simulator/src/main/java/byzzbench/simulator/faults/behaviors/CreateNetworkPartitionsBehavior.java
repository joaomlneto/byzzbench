package byzzbench.simulator.faults.behaviors;

import byzzbench.simulator.domain.FaultInjectionAction;
import byzzbench.simulator.faults.FaultBehavior;
import byzzbench.simulator.faults.ScenarioContext;
import byzzbench.simulator.transport.Router;

import java.util.Arrays;
import java.util.Collection;

public class CreateNetworkPartitionsBehavior implements FaultBehavior {
    private final String[][] partitions;

    /**
     * Create a new CreateNetworkPartitionsBehavior
     *
     * @param partitions A list of partitions to create
     */
    public CreateNetworkPartitionsBehavior(String[][] partitions) {
        this.partitions = partitions;
    }

    /**
     * Create a new CreateNetworkPartitionsBehavior
     *
     * @param partition A single partition to create
     */
    public CreateNetworkPartitionsBehavior(String[] partition) {
        this.partitions = new String[][]{partition};
    }

    /**
     * Create a new CreateNetworkPartitionsBehavior
     *
     * @param partition A single partition to create
     */
    public CreateNetworkPartitionsBehavior(Collection<String> partition) {
        this(partition.toArray(new String[0]));
    }

    /**
     * Create a new CreateNetworkPartitionsBehavior
     *
     * @param partition The ID of the node to isolate
     */
    public CreateNetworkPartitionsBehavior(String partition) {
        this(new String[]{partition});
    }

    @Override
    public String getId() {
        return "createnetworkpartitions-%s".formatted(Arrays.toString(this.partitions));
    }

    @Override
    public String getName() {
        // convert the array of arrays into a string
        String partitions = Arrays.stream(this.partitions)
                .map(Arrays::toString)
                .reduce("", (a, b) -> a + b);

        return "Create partition %s".formatted(partitions);
    }

    @Override
    public FaultInjectionAction toAction(ScenarioContext context) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void accept(ScenarioContext context) {
        Router router = context.getScenario().getTransport().getRouter();
        for (String[] partition : partitions) {
            router.isolateNodes(partition);
        }
    }
}
