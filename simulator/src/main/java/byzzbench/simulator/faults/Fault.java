package byzzbench.simulator.faults;

import byzzbench.simulator.utils.NonNull;

import java.io.Serializable;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * An interface representing a fault, which is a combination of a {@link Predicate}
 * which checks if the fault can be applied to a message, and a {@link Consumer}, which
 * applies the faulty behavior.
 */
public interface Fault extends Predicate<ScenarioContext>, FaultBehavior, Serializable {
    /**
     * Gets the unique id of the fault.
     *
     * @return the id of the fault
     */
    @NonNull
    String getId();

    /**
     * Gets a human-readable name of the fault.
     *
     * @return the name of the fault
     */
    @NonNull
    String getName();

    /**
     * Checks if the fault can be applied to the given state
     *
     * @param state the state of the system
     * @return True if the fault can be applied, false otherwise
     */
    @Override
    boolean test(ScenarioContext state);

    /**
     * Applies a fault to the state of the system
     *
     * @param state the state of the system
     */
    @Override
    void accept(ScenarioContext state);

    /**
     * Checks if the fault can be applied to the given state and applies it if it can
     *
     * @param state the state of the system
     * @return True if the fault was applied, false otherwise
     */
    default boolean testAndAccept(ScenarioContext state) {
        if (test(state)) {
            accept(state);
            return true;
        }
        return false;
    }
}
