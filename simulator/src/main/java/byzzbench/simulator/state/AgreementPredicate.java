package byzzbench.simulator.state;

import byzzbench.simulator.BaseScenario;
import byzzbench.simulator.Replica;
import byzzbench.simulator.ScenarioPredicate;

import java.util.Collection;
import java.util.Objects;

public class AgreementPredicate implements ScenarioPredicate {
    @Override
    public boolean test(BaseScenario scenarioExecutor) {
        Collection<Replica> replicas = scenarioExecutor.getTransport().getNodes().values();

        // get the max length of the commit logs of all replicas
        int commonPrefixLength = replicas.stream()
                .map(replica -> replica.getCommitLog().getLength())
                .max(Integer::compareTo)
                .orElse(0);

        // check if the Nth entry in the commit log of each replica is the same
        for (int i = 0; i < commonPrefixLength; i++) {
            final int index = i;
            if (replicas.stream()
                    .map(replica ->replica.getCommitLog().get(index))
                    .filter(Objects::nonNull)
                    .distinct()
                    .count() > 1) {
                return false;
            }
        }

        return true;
    }
}
