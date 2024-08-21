package byzzbench.simulator.faults;

import byzzbench.simulator.transport.Router;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * A pseudo-fault that heals the network completely
 */
@Getter
@ToString
@RequiredArgsConstructor
public class HealNodeNetworkFault implements Fault {
    @NonNull
    private final String nodeId;

    public String getId() {
        return "HealProcessNetworkFault(%s)".formatted(this.nodeId);
    }

    public String getName() {
        return "Re-join %s to Network".formatted(this.nodeId);
    }

    /**
     * Checks if the specific node is not in the default partition
     * @param ctx the input argument
     * @return True if the specific node is not in the default partition, false otherwise
     */
    @Override
    public final boolean test(FaultInput ctx) {
        Router router = ctx.getScenario().getTransport().getRouter();
        return router.getNodePartition(nodeId) != Router.DEFAULT_PARTITION;
    }

    /**
     * Re-joins the specific node to the rest of the network
     * @param state the input argument
     */
    @Override
    public void accept(FaultInput state) {
        Router router = state.getScenario().getTransport().getRouter();
        router.healNode(nodeId);
    }
}
