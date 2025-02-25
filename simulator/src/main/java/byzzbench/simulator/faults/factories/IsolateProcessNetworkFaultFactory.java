package byzzbench.simulator.faults.factories;

import byzzbench.simulator.Replica;
import byzzbench.simulator.faults.Fault;
import byzzbench.simulator.faults.FaultContext;
import byzzbench.simulator.faults.FaultFactory;
import byzzbench.simulator.faults.faults.HealNetworkFault;
import byzzbench.simulator.faults.faults.HealNodeNetworkFault;
import byzzbench.simulator.faults.faults.IsolateProcessNetworkFault;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * A factory that creates IsolateProcessNetworkFaults for each {@link Replica} in the network,
 * allowing each replica to be isolated from the network.
 */
@Component
public class IsolateProcessNetworkFaultFactory implements FaultFactory {
    @Override
    public List<Fault> generateFaults(FaultContext input) {
        List<Fault> networkFaults = new ArrayList<>();

        // create a IsolateProcessNetworkFault for each replica in the network
        networkFaults.addAll(input.getScenario().getReplicas().navigableKeySet().stream()
                .map(IsolateProcessNetworkFault::new)
                .toList());

        // create heal faults for each replica in the network
        networkFaults.addAll(input.getScenario().getReplicas().navigableKeySet().stream()
                .map(HealNodeNetworkFault::new)
                .toList());

        // create a HealNetworkFault
        networkFaults.add(new HealNetworkFault());

        return networkFaults;
    }
}
