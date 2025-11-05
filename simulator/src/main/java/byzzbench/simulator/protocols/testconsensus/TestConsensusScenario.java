package byzzbench.simulator.protocols.testconsensus;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.domain.ScenarioParameters;
import byzzbench.simulator.domain.Schedule;
import byzzbench.simulator.nodes.Client;
import byzzbench.simulator.nodes.Replica;
import byzzbench.simulator.protocols.pbft_java.PbftClient;
import byzzbench.simulator.state.CommitLog;
import byzzbench.simulator.state.LogEntry;
import lombok.Getter;
import lombok.extern.java.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * A tiny consensus protocol scenario for testing exploration strategies and faults.
 * Replicas exchange simple messages (Propose/Ack/Commit) and append committed values
 * to their {@link CommitLog}. Correctness can be checked by comparing replica logs.
 */
@Getter
@Log
public class TestConsensusScenario extends Scenario {

    public TestConsensusScenario(Schedule schedule) {
        super(schedule);
    }

    @Override
    public void loadScenarioParameters(ScenarioParameters parameters) {
        // No special parameters; number of replicas/clients can be provided via ScenarioParameters
    }

    @Override
    protected void setup() {
        try {
            int n = Optional.ofNullable(this.getSchedule().getParameters().getNumReplicas()).orElse(3);
            for (int i = 0; i < n; i++) {
                String id = Character.toString((char) ('A' + i));
                Replica replica = new TestConsensusReplica(id, this);
                this.addNode(replica);
            }
            // Add a simple client (not used by this protocol, but satisfies tooling expectations)
            String clientId = "C1";
            Client client = new PbftClient(this, clientId);
            this.addClient(client);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized void run() {
        // No active background behavior needed.
    }

    @Override
    public int maxFaultyReplicas(int n) {
        // Majority protocol: tolerate floor((n-1)/2)
        return Math.max(0, (n - 1) / 2);
    }

    @Override
    public Class<? extends Replica> getReplicaClass() {
        return TestConsensusReplica.class;
    }

    @Override
    public Class<? extends Client> getClientClass() {
        return PbftClient.class;
    }

    /**
     * Helper for tests: returns true if all replicas have identical commit logs
     * (length and each slot's value are equal).
     */
    public boolean logsMatch() {
        List<Replica> reps = new ArrayList<>(this.getReplicas().values());
        if (reps.isEmpty()) return true;
        CommitLog ref = reps.get(0).getCommitLog();
        for (int i = 1; i < reps.size(); i++) {
            CommitLog other = reps.get(i).getCommitLog();
            if (ref.getLength() != other.getLength()) return false;
            long low = ref.getLowestSequenceNumber();
            long high = ref.getHighestSequenceNumber();
            for (long s = low; s <= high; s++) {
                LogEntry a = ref.get(s);
                LogEntry b = other.get(s);
                if (a == null || b == null) return false;
                if (!Objects.equals(a, b)) return false;
            }
        }
        return true;
    }
}
