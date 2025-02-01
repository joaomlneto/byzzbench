package byzzbench.simulator.state;

import byzzbench.simulator.Replica;
import byzzbench.simulator.Scenario;
import byzzbench.simulator.ScenarioPredicate;

import java.util.Collection;

public class IntegrityPredicate implements ScenarioPredicate {
    @Override
    public String getId() {
        return "Integrity";
    }

    @Override
    public boolean test(Scenario scenarioExecutor) {
        Collection<Replica> replicas = scenarioExecutor.getReplicas().values().stream()
                .filter(node -> !scenarioExecutor.isFaultyReplica(node.getId()))
                .toList();


        // check for duplicate entries in each replica's commit log
        for (Replica replica : replicas) {
            CommitLog commitLog = replica.getCommitLog();
            for (int i = 0; i < commitLog.getLength(); i++) {
                for (int j = i + 1; j < commitLog.getLength(); j++) {
                    if (commitLog.get(i) == null || commitLog.get(j) == null) {
                        continue;
                    }
                    if (commitLog.get(i).equals(commitLog.get(j))) {
                        System.out.println("IntegrityPredicate: Duplicate vote!");
                        System.out.println("Replica " + replica.getId() + " has duplicate entries at indices " + i + " and " + j);
                        return false; 
                    }
                }
            }
        }

        return true;
    }
}
