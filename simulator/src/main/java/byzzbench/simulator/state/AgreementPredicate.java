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
        Collection<Replica> replicas = scenarioExecutor.getNodes().values().stream()
                .filter(Replica.class::isInstance)
                .map(Replica.class::cast)
                .toList();

        // get the max length of the commit logs of all replicas
        int commonPrefixLength = replicas.stream()
                .map(replica -> replica.getCommitLog().getLength())
                .max(Integer::compareTo)
                .orElse(0);

        // check if the Nth entry in the commit log of each replica is the same
        for (int i = 0; i < commonPrefixLength; i++) {
            final int index = i;
            List<LogEntry> distinctIthEntries = replicas.stream()
                    .map(Replica::getCommitLog)
                    .map(log -> log.getLength() > index ? log.get(index) : null)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();
            if (distinctIthEntries.size() > 1) {
                System.out.println("AgreementPredicate: Disagreement at index " + i);
                System.out.println("REPLICAS:");
                for (Replica replica : replicas) {
                    try {
                        System.out.println(replica.getCommitLog().get(index));
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
