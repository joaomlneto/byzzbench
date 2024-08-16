package byzzbench.simulator.faults;

import byzzbench.simulator.transport.Router;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

/**
 * A pseudo-fault that heals the network completely
 */
@Getter
@ToString
@RequiredArgsConstructor
public class HealNetworkFault<T extends Serializable> implements Fault<T> {
    public String getId() {
        return "HealNetwork";
    }

    public String getName() {
        return "Heal Network";
    }

    /**
     * Checks if the network is not already healed
     * @param ctx the input argument
     * @return True if the network is not already healed, false otherwise
     */
    @Override
    public final boolean test(FaultInput<T> ctx) {
        Router router = ctx.getScenario().getTransport().getRouter();
        return router.hasActivePartitions();
    }

    /**
     * Heal the network completely
     * @param state the input argument
     */
    @Override
    public void accept(FaultInput<T> state) {
        Router router = state.getScenario().getTransport().getRouter();
        router.resetPartitions();
    }
}
