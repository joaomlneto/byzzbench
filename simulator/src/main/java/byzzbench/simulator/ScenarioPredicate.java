package byzzbench.simulator;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.util.Comparator;
import java.util.function.Predicate;

/**
 * A predicate for scenarios. Used to evaluate whether a scenario meets certain conditions.
 */
@RequiredArgsConstructor
public abstract class ScenarioPredicate implements Serializable, Predicate<Scenario>, Comparable<ScenarioPredicate> {
    /**
     * The scenario to evaluate.
     */
    @JsonIgnore
    @Getter
    private final Scenario scenario;

    /**
     * Get the unique identifier of the predicate.
     *
     * @return The unique identifier.
     */
    public String getId() {
        return this.getClass().getSimpleName();
    }

    @Override
    public int compareTo(ScenarioPredicate other) {
        return Comparator.comparing(ScenarioPredicate::getId).compare(this, other);
    }

    /**
     * Evaluate the predicate on the given scenario.
     *
     * @return True if the scenario satisfies the predicate, false otherwise.
     */
    @JsonGetter("satisfied")
    public boolean test() {
        return test(scenario);
    }

    /**
     * Provide an explanation for the predicate result.
     *
     * @return A string explanation of the result.
     */
    public String getExplanation() {
        return test() ? "OK" : "Failed";
    }

}
