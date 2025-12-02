package byzzbench.simulator.protocols.testconsensus;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.nodes.Replica;
import byzzbench.simulator.state.LogEntry;
import byzzbench.simulator.state.SerializableLogEntry;
import byzzbench.simulator.state.TotalOrderCommitLog;
import byzzbench.simulator.transport.MessagePayload;
import lombok.Getter;
import lombok.extern.java.Log;

import java.time.Duration;
import java.util.*;

import static byzzbench.simulator.protocols.testconsensus.TestConsensusMessages.*;

/**
 * A very small consensus-like replica for testing: a leader proposes values for slots, followers ACK, and
 * the leader commits upon receiving a majority of ACKs, broadcasting a Commit. All replicas append the
 * committed value at the given slot to their commit log.
 * <p>
 * Safety condition for tests: all replicas' commit logs should match after all messages are delivered.
 */
@Log
public class TestConsensusReplica extends Replica {

    @Getter
    private final String id;

    private final Scenario scenario;

    // Leader state: ackers per slot
    private final Map<Long, Set<String>> ackers = new HashMap<>();
    // Follower state: track last seen proposal per slot to avoid duplicate acks
    private final Map<Long, String> seenProposals = new HashMap<>();

    public TestConsensusReplica(String id, Scenario scenario) {
        super(id, scenario, new TotalOrderCommitLog());
        this.id = id;
        this.scenario = scenario;
    }

    @Override
    public void initialize() {
        // Optionally, leader can self-schedule a tiny startup tick to propose a first value for slot 1
        if (isLeader()) {
            // schedule a small timeout to trigger initial proposal (so tests can progress deterministically)
            setTimeout("init-propose", this::proposeNextDefault, Duration.ZERO);
        }
    }

    private boolean isLeader() {
        // Define leader as lexicographically smallest replica id
        return this.getNodeIds().first().equals(this.id);
    }

    private int quorumSize() {
        int n = this.getNodeIds().size();
        return (n / 2) + 1; // majority quorum
    }

    private long nextSlot() {
        // Commit log in TotalOrderCommitLog starts from INITIAL_SEQUENCE_NUMBER (0), add next
        return this.getCommitLog().getHighestSequenceNumber() + 1;
    }

    private void proposeNextDefault() {
        long slot = nextSlot();
        String value = "val-" + slot;
        propose(slot, value);
    }

    public void propose(long slot, String value) {
        if (!isLeader()) return; // only leader proposes in this simple protocol
        // Start ack set with leader's own implicit ack
        ackers.computeIfAbsent(slot, s -> new HashSet<>()).add(this.id);
        Propose p = new Propose(slot, value);
        log.fine(() -> String.format("%s proposing slot=%d value=%s", id, slot, value));
        broadcastMessage(p);
        // Leader also records seen proposal like followers would (so it will accept its own commit)
        seenProposals.putIfAbsent(slot, value);
        // Check if single node (quorum achieved already)
        maybeCommit(slot, value);
    }

    private void onPropose(String sender, Propose p) {
        // Followers record proposal and reply with ACK once
        String prev = seenProposals.putIfAbsent(p.getSlot(), p.getValue());
        if (prev == null) {
            Ack ack = new Ack(p.getSlot(), p.getValue());
            sendMessage(ack, leaderId());
        } else {
            // If we already saw the same value, still ensure an ACK was sent at least once
            if (Objects.equals(prev, p.getValue())) {
                // no-op; assume previous ACK delivered or will be delivered
            }
        }
    }

    private void onAck(String sender, Ack ack) {
        if (!isLeader()) return; // only leader collects ACKs
        // Only count acks for the currently proposed value for this slot
        ackers.computeIfAbsent(ack.getSlot(), s -> new HashSet<>()).add(sender);
        // Also account for leader self-ack if not present
        ackers.get(ack.getSlot()).add(this.id);
        maybeCommit(ack.getSlot(), ack.getValue());
    }

    private void maybeCommit(long slot, String value) {
        if (!isLeader()) return;
        Set<String> set = ackers.getOrDefault(slot, Collections.emptySet());
        if (set.size() >= quorumSize()) {
            // Broadcast commit
            Commit c = new Commit(slot, value);
            broadcastMessageIncludingSelf(c);
        }
    }

    private void onCommit(String sender, Commit c) {
        // Apply commit if not yet committed at this slot
        LogEntry le = new SerializableLogEntry(c.getValue());
        // Only add if the slot is strictly higher than current highest, or matches and absent.
        // TotalOrderCommitLog.add(sequence, entry) will throw if duplicate, so guard duplicates.
        if (this.getCommitLog().get(c.getSlot()) == null) {
            this.commitOperation(c.getSlot(), le);
            log.fine(() -> String.format("%s committed slot=%d value=%s", id, c.getSlot(), c.getValue()));
        }
    }

    private String leaderId() {
        return this.getNodeIds().first();
    }

    @Override
    public void handleMessage(String sender, MessagePayload message) {
        if (message instanceof Propose p) {
            onPropose(sender, p);
        } else if (message instanceof Ack a) {
            onAck(sender, a);
        } else if (message instanceof Commit c) {
            onCommit(sender, c);
        } else {
            log.warning(() -> String.format("%s received unknown message type %s from %s", id, message.getType(), sender));
        }
    }
}
