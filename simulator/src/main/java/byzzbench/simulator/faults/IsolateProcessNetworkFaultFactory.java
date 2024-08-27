package byzzbench.simulator.faults;

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
        networkFaults.addAll(input.getScenario().getTransport().getNodeIds().stream()
                .map(IsolateProcessNetworkFault::new)
                .toList());

        // create heal faults for each node in the network
        networkFaults.addAll(input.getScenario().getTransport().getNodeIds().stream()
                .map(HealNodeNetworkFault::new)
                .toList());

        // create a HealNetworkFault
        networkFaults.add(new HealNetworkFault());

        return networkFaults;
    }
}
