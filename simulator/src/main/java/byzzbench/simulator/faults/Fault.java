package byzzbench.simulator.faults;

import byzzbench.simulator.domain.Action;
import byzzbench.simulator.utils.NonNull;

import java.io.Serializable;
import java.util.Comparator;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * An interface representing a fault, which is a combination of a {@link Predicate}
 * which checks if the fault can be applied to a message, and a {@link Consumer}, which
 * applies the faulty behavior.
 */
public abstract class Fault implements Predicate<ScenarioContext>, FaultBehavior, Serializable, Comparable<Fault> {
    /**
     * Gets the unique id of the fault.
     *
     * @return the id of the fault
     */
    @NonNull
    public abstract String getId();

    /**
     * Gets a human-readable name of the fault.
     *
     * @return the name of the fault
     */
    @NonNull
    public abstract String getName();

    /**
     * Checks if the fault can be applied to the given state
     *
     * @param state the state of the system
     * @return True if the fault can be applied, false otherwise
     */
    @Override
    public abstract boolean test(ScenarioContext state);

    /**
     * Checks if the fault can be applied to the given state and applies it if it can
     *
     * @param state the state of the system
     * @return True if the fault was applied, false otherwise
     */
    public boolean testAndAccept(ScenarioContext state) {
        if (test(state)) {
            Action action = this.toAction(state);
            action.accept(state.getScenario());
            return true;
        }
        return false;
    }

    // implement the compareTo
    public int compareTo(@NonNull Fault other) {
        return Comparator.comparing(Fault::getId).compare(this, other);
    }
}
