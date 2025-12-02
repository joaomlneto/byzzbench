package byzzbench.simulator;

import byzzbench.simulator.nodes.Client;
import byzzbench.simulator.nodes.Replica;

/**
 * An observer that listens to changes in the scenario.
 */
public interface ScenarioObserver {
    /**
     * Called when a replica is added to the scenario.
     *
     * @param replica The replica that was added.
     */
    void onReplicaAdded(Replica replica);

    /**
     * Called when a client is added to the scenario.
     *
     * @param client The client that was added.
     */
    void onClientAdded(Client client);
}
