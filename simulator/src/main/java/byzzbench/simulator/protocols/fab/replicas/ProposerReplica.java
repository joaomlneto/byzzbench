package byzzbench.simulator.protocols.fab.replicas;

import byzzbench.simulator.protocols.fab.ProgressCertificate;
import byzzbench.simulator.protocols.fab.messages.AcceptMessage;
import byzzbench.simulator.protocols.fab.messages.LearnMessage;
import byzzbench.simulator.protocols.pbft_java.MessageLog;
import byzzbench.simulator.transport.MessagePayload;
import byzzbench.simulator.transport.Transport;
import lombok.Getter;
import lombok.extern.java.Log;

import java.util.*;

/**
 * A <h1>ProposerReplica</h1> class that represents the proposer replica in the FAB protocol.
 * The proposer is responsible for proposing a value to the acceptors.
 * The proposer waits for the majority of the acceptors to accept the value before sending a LEARN message to the learners.
 * The proposer then waits for the majority of the learners to learn the value.
 * The proposer then sends the value to the client.
 */
@Log
@Getter
public class ProposerReplica extends FabReplica {
    private final FabRole role;
    private final Map<String, Pair> satisfied;
    private final Set<String> learned;
    private long proposalNumber;
    private String proposedValue;
    private ProgressCertificate pc;

    public ProposerReplica(String nodeId, SortedSet<String> nodeIds, Transport transport, MessageLog messageLog) {
        super(nodeId, nodeIds, transport, messageLog);
        this.role = FabRole.PROPOSER;
        this.satisfied = new HashMap<>();
        this.learned = new HashSet<>();
        proposalNumber = 0;
        proposedValue = null;
    }

    public void onStart() {

    }

    @Override
    public void handleMessage(String sender, MessagePayload message) throws Exception {
        if (message instanceof AcceptMessage) {
            handleAcceptMessage(sender, (AcceptMessage) message);
        } else if (message instanceof LearnMessage) {
            handleLearnMessage(sender, (LearnMessage) message);
        }
    }

    public void handleAcceptMessage(String sender, AcceptMessage message) {
        satisfied.put(sender, new Pair(message.getReplicaId(), message.getValue()));
        // Check if the proposer has received enough ACCEPT messages for the same (round, value) pair
        if (satisfied.size() > getNodeIds().size() / 2) {
            Pair max = satisfied.values().stream().max(Comparator.comparing(Pair::getValue)).get();
            if (Collections.frequency(satisfied.values(), max) > getNodeIds().size() / 2) {
                // Send a LEARN message to all LEARNER replicas
                for (String replicaId : getNodeIds()) {
                    if (!replicaId.equals(getNodeId()) && !learned.contains(replicaId)) {
                        getTransport().sendMessage(this.getNodeId(), new LearnMessage(), replicaId);
                    }
                }
            }
        }
    }

    public void handleLearnMessage(String sender, LearnMessage message) {
        learned.add(sender);
        if (learned.size() > getNodeIds().size() / 2) {
            // The proposer has learned the value
            System.out.println("Proposer " + getNodeId() + " has learned the value " + proposedValue);
        }
    }
}
