package byzzbench.simulator.faults;

import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.function.Predicate;

public interface FaultPredicate extends Predicate<FaultContext>, Serializable {
    String getId();

    String getName();

    /**
     * Combines this predicate with another predicate using a logical AND.
     *
     * @param other The other predicate to combine with
     * @return A new predicate that is the logical AND of this predicate and the other predicate
     */
    default FaultPredicate and(FaultPredicate other) {
        return new FaultPredicate() {
            @Override
            public String getId() {
                return FaultPredicate.this.getId() + "&&" + other.getId();
            }

            @Override
            public String getName() {
                return FaultPredicate.this.getName() + " and " + other.getName();
            }

            @Override
            public boolean test(FaultContext ctx) {
                return FaultPredicate.this.test(ctx) && other.test(ctx);
            }
        };
    }

    /**
     * Combines this predicate with another predicate using a logical OR.
     *
     * @param other The other predicate to combine with
     * @return A new predicate that is the logical OR of this predicate and the other predicate
     */
    default FaultPredicate or(FaultPredicate other) {
        return new FaultPredicate() {
            @Override
            public String getId() {
                return FaultPredicate.this.getId() + "||" + other.getId();
            }

            @Override
            public String getName() {
                return FaultPredicate.this.getName() + " or " + other.getName();
            }

            @Override
            public boolean test(FaultContext ctx) {
                return FaultPredicate.this.test(ctx) || other.test(ctx);
            }
        };
    }

    /**
     * Negates this predicate.
     *
     * @return A new predicate that is the negation of this predicate
     */
    @Override
    default @NotNull FaultPredicate negate() {
        return new FaultPredicate() {
            @Override
            public String getId() {
                return "!(" + FaultPredicate.this.getId() + ")";
            }

            @Override
            public String getName() {
                return "not " + FaultPredicate.this.getName();
            }

            @Override
            public boolean test(FaultContext ctx) {
                return !FaultPredicate.this.test(ctx);
            }
        };
    }
}
