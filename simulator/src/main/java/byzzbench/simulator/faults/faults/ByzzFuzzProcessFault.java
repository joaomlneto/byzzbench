package byzzbench.simulator.faults.faults;

import byzzbench.simulator.faults.BaseFault;
import byzzbench.simulator.faults.ReplicaFault;
import byzzbench.simulator.faults.RoundBasedFault;
import byzzbench.simulator.faults.behaviors.MutateMessageBehavior;
import byzzbench.simulator.faults.predicates.MessageRecipientHasIdPredicate;
import byzzbench.simulator.faults.predicates.MessageRoundPredicate;
import byzzbench.simulator.faults.predicates.MessageSenderHasIdPredicate;
import lombok.Getter;

import java.util.Set;

/**
 * Creates a process fault that simulates a byzantine process fault when delivering
 * messages that contain a round number. If the message matches the sender, receiver and
 * round number of the fault, a random mutation is applied to the message.
 */
public class ByzzFuzzProcessFault extends BaseFault implements RoundBasedFault, ReplicaFault {

    @Getter
    private final long round;

    private final String senderId;

    /**
     * Create a new ByzzFuzzProcessFault
     *
     * @param recipients The recipients of the message
     * @param sender     The sender of the message
     * @param round      The round during which to create the fault
     */
    public ByzzFuzzProcessFault(Set<String> recipients, String sender, int round) {
        super(
                "byzzfuzzprocessfault-%d-%s-%s".formatted(round, sender, String.join("-", recipients)),
                new MessageRoundPredicate(round)
                        .and(new MessageSenderHasIdPredicate(sender))
                        .and(new MessageRecipientHasIdPredicate(recipients)),
                new MutateMessageBehavior()
        );
        this.round = round;
        this.senderId = sender;
    }

    public String getFaultyReplicaId() {
        return senderId;
    }
}
