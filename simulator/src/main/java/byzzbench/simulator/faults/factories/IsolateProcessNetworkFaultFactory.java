package byzzbench.simulator.faults.factories;

import byzzbench.simulator.faults.Fault;
import byzzbench.simulator.faults.FaultContext;
import byzzbench.simulator.faults.FaultFactory;
import byzzbench.simulator.faults.faults.HealNetworkFault;
import byzzbench.simulator.faults.faults.HealNodeNetworkFault;
import byzzbench.simulator.faults.faults.IsolateProcessNetworkFault;

import java.util.ArrayList;
import java.util.List;

/**
 * A factory that creates IsolateProcessNetworkFaults for each node in the network,
 * allowing each node to be isolated from the network.
 */
public class IsolateProcessNetworkFaultFactory implements FaultFactory {
    @Override
    public List<Fault> generateFaults(FaultContext input) {
        List<Fault> networkFaults = new ArrayList<>();

        // create a IsolateProcessNetworkFault for each node in the network
        networkFaults.addAll(input.getScenario().getNodes().navigableKeySet().stream()
                .map(IsolateProcessNetworkFault::new)
                .toList());

        // create heal faults for each node in the network
        networkFaults.addAll(input.getScenario().getNodes().navigableKeySet().stream()
                .map(HealNodeNetworkFault::new)
                .toList());

        // create a HealNetworkFault
        networkFaults.add(new HealNetworkFault());

        return networkFaults;
    }
}
