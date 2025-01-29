package byzzbench.simulator.state;

import byzzbench.simulator.Replica;
import byzzbench.simulator.Scenario;
import byzzbench.simulator.ScenarioPredicate;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class AgreementPredicate implements ScenarioPredicate {
    @Override
    public String getId() {
        return "Agreement";
    }

    @Override
    public boolean test(Scenario scenarioExecutor) {
        Collection<Replica> replicas = scenarioExecutor.getReplicas().values().stream()
                .filter(node -> !scenarioExecutor.isFaultyReplica(node.getId()))
                .toList();

        // get the max length of the commit logs of all replicas
        long lowestSequenceNumber = replicas.stream()
                .map(replica -> replica.getCommitLog().getLowestSequenceNumber())
                .min(Long::compareTo)
                .orElse(0L);
        long highestSequenceNumber = replicas.stream()
                .map(replica -> replica.getCommitLog().getHighestSequenceNumber())
                .max(Long::compareTo)
                .orElse(0L);

        // check if the Nth entry in the commit log of each replica is the same
        for (long i = lowestSequenceNumber; i <= highestSequenceNumber; i++) {
            final long index = i;
            List<LogEntry> distinctIthEntries = replicas.stream()
                    .map(Replica::getCommitLog)
                    .map(log -> log.get(index))
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();
            if (distinctIthEntries.size() > 1) {
                System.out.println("AgreementPredicate: Disagreement at index " + i);
                System.out.println("REPLICAS:");
                for (Replica replica : replicas) {
                    try {
                        System.out.println("Replica " + replica.getId() + " " + replica.getCommitLog().get(index));
                    } catch (Exception e) {
                        //e.printStackTrace();
                    }
                }
                System.out.println("DISTINCT ENTRIES:");
                for (LogEntry entry : distinctIthEntries) {
                    System.out.println(entry);
                }
                return false;
            }
        }

        return true;
    }
}
