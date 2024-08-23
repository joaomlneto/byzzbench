package byzzbench.simulator.utils;

import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.NotNull;
import java.lang.annotation.*;

/**
 * Declares that the annotated element cannot be null.
 *
 * <p>Applying this annotation to a method parameter is equivalent to
 * applying the following annotations to the method parameter:
 * <ul>
 *     <li>{@link lombok.NonNull}</li>
 *     <li>{@link jakarta.annotation.Nonnull}</li>
 *     <li>{@link jakarta.validation.constraints.NotNull}</li>
 * </ul>
 */
@Target({ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Nonnull
@lombok.NonNull
@NotNull
public @interface NonNull {}
