package byzzbench.simulator.scheduler.twins;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.transport.Transport;

/**
 * A transport layer that is used by the {@link TwinsReplica} to route messages
 * internally between the twin replica instances.
 */
public class TwinsTransport extends Transport {
    public TwinsTransport(Scenario scenario) {
        super(scenario);
    }
}
