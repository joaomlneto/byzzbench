package byzzbench.simulator.faults.predicates;

import byzzbench.simulator.faults.FaultPredicate;
import byzzbench.simulator.faults.ScenarioContext;

import java.util.List;

public class ANDPredicate implements FaultPredicate {
    private final List<FaultPredicate> predicates;

    public ANDPredicate(FaultPredicate... predicates) {
        this.predicates = List.of(predicates);
    }

    @Override
    public String getId() {
        return "(%s)".formatted(predicates.stream()
                .map(FaultPredicate::getId)
                .reduce("", (a, b) -> a + "," + b));
    }

    @Override
    public String getName() {
        final String glue = " AND ";
        return predicates.stream()
                .map(FaultPredicate::getName)
                .reduce("", (a, b) -> a + glue + b).substring(glue.length());
    }

    @Override
    public boolean test(ScenarioContext scenarioContext) {
        return predicates.stream().allMatch(p -> p.test(scenarioContext));
    }
}
