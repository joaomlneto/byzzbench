package byzzbench.simulator.protocols.tendermint;

import byzzbench.simulator.BaseScenario;
import byzzbench.simulator.Replica;
//import byzzbench.simulator.TerminationCondition;
import byzzbench.simulator.protocols.pbft_java.PbftTerminationPredicate;
import byzzbench.simulator.protocols.tendermint.MessageLog;
import byzzbench.simulator.scheduler.Scheduler;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.java.Log;

import java.util.*;

@Log
public class TendermintScenarioExecutor extends BaseScenario {
    private final int NUM_NODES = 4;

    public TendermintScenarioExecutor(Scheduler scheduler) {
        super("tendermint", scheduler);
        this.terminationCondition = new TendermintTerminationPredicate();
    }

    @Override
    public void loadScenarioParameters(JsonNode parameters) {
        // no parameters to load
    }

    @Override
    protected void setup() {
        try {
            SortedSet<String> nodeIds = new TreeSet<>();
            for (int i = 0; i < NUM_NODES; i++) {
                nodeIds.add(Character.toString((char) ('A' + i)));
            }

            long tolerance = nextLong( 10);
            long totalVotingPower = 3 * tolerance + 1;

            List<Long> votingPowers = distributeVotingPower(NUM_NODES, totalVotingPower);

            Iterator<String> nodeIdIterator = nodeIds.iterator();
            while (nodeIdIterator.hasNext()) {
                String nodeId = nodeIdIterator.next();
                Replica replica = new TendermintReplica(nodeId, nodeIds, this, tolerance, votingPowers);

                this.addNode(replica);
            }

            log.info("Assigned Voting Power: " + votingPowers);
            log.info(this.getNodes().toString());

            this.setNumClients(1);
            log.info(this.getNodes().toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public long nextLong(long bound) {
        return Math.abs(new Random().nextLong(bound));
    }

    public static List<Long> distributeVotingPower(int numNodes, long totalVotingPower) {
        Random random = new Random(2137L);
        List<Long> votingPowers = new ArrayList<>();

        // Assign 1 voting power to each node initially
        for (int i = 0; i < numNodes; i++) {
            votingPowers.add(1L);
        }

        // Subtract the initial allocation from the total voting power
        long remainingVotingPower = totalVotingPower - numNodes;

        // Distribute the remaining voting power randomly
        while (remainingVotingPower > 0) {
            int index = random.nextInt(numNodes); // Pick a random node
            votingPowers.set(index, votingPowers.get(index) + 1); // Add 1 to its voting power
            remainingVotingPower--;
        }

        return votingPowers;
    }


    @Override
    public synchronized void run() {
//        // send a request message to node A
//        try {
//            this.setNumClients(1);
//            this.transport.sendClientRequest("C0", "123", "A");
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw new RuntimeException(e);
//        }
    }
}
