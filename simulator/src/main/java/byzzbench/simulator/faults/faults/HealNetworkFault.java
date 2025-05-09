package byzzbench.simulator.faults.faults;

import byzzbench.simulator.faults.Fault;
import byzzbench.simulator.faults.ScenarioContext;
import byzzbench.simulator.transport.Router;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * A pseudo-fault that heals the network completely
 */
@Getter
@ToString
@RequiredArgsConstructor
public class HealNetworkFault implements Fault {
    public String getId() {
        return "HealNetwork";
    }

    public String getName() {
        return "Heal Network";
    }

    /**
     * Checks if the network is not already healed
     *
     * @param ctx the input argument
     * @return True if the network is not already healed, false otherwise
     */
    @Override
    public final boolean test(ScenarioContext ctx) {
        Router router = ctx.getScenario().getTransport().getRouter();
        return router.hasActivePartitions();
    }

    /**
     * Heal the network completely
     *
     * @param state the input argument
     */
    @Override
    public void accept(ScenarioContext state) {
        Router router = state.getScenario().getTransport().getRouter();
        router.resetPartitions();
    }
}
