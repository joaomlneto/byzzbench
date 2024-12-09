package byzzbench.simulator.utils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * A {@link CompletableFuture} that uses a single thread {@link Executor} to guarantee ordering of dependent tasks.
 * Dependent tasks are executed in the order they are added (FIFO).
 *
 * @param <T> The result type returned by this future's {@code join} and {@code get} methods
 */
public class FIFOCompletableFuture<T> extends CompletableFuture<T> {
    private static final Executor executor = Executors.newSingleThreadExecutor(
            runnable -> new Thread(runnable, "Custom-Single-Thread")
    );

    @Override
    public final Executor defaultExecutor() {
        return executor;
    }


}
