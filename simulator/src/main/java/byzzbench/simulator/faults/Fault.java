package byzzbench.simulator.faults;

import java.io.Serializable;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * An interface representing a fault, which is a combination of a {@link Predicate}
 * which checks if the fault can be applied to a message, and a {@link Consumer}, which
 * applies the faulty behavior.
 */
public interface Fault extends Predicate<FaultInput>, FaultBehavior, Serializable {
    /**
     * Gets the unique id of the fault.
     * @return the id of the fault
     */
    String getId();

    /**
     * Gets a human-readable name of the fault.
     * @return the name of the fault
     */
    String getName();

    /**
     * Checks if the fault can be applied to the given state
     * @param state the state of the system
     * @return True if the fault can be applied, false otherwise
     */
    @Override
    boolean test(FaultInput state);

    /**
     * Applies a fault to the state of the system
     * @param state the state of the system
     */
    @Override
    void accept(FaultInput state);
}
