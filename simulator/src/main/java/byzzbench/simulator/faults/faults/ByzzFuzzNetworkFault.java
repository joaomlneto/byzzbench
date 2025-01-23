package byzzbench.simulator.faults.faults;

import byzzbench.simulator.faults.BaseFault;
import byzzbench.simulator.faults.RoundBasedFault;
import byzzbench.simulator.faults.behaviors.CreateNetworkPartitionsBehavior;
import byzzbench.simulator.faults.behaviors.ByzzFuzzDropMessageBehavior;
import byzzbench.simulator.faults.predicates.MessageRoundPredicate;
import lombok.Getter;

import java.util.Set;

/**
 * Creates a network fault that simulates a network partition when delivering
 * messages that contain a round number. If the round number of the message
 * matches the round number of the fault, and the sender and receiver of the message
 * are not in the same partition, the message will be dropped.
 */
public class ByzzFuzzNetworkFault extends BaseFault implements RoundBasedFault {

    @Getter
    private final long round;
    /**
     * Create a new ByzzFuzzNetworkFault
     *
     * @param partition The partition to create
     * @param round     The round during which to create the partition
     */
    public ByzzFuzzNetworkFault(Set<String> partition, int round) {
        super(
                "byzzfuzznetworkfault-%d-%s".formatted(round, String.join("-", partition)),
                new MessageRoundPredicate(round),
                new ByzzFuzzDropMessageBehavior(partition)
        );
        this.round = round;
    }
}
