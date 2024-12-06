package byzzbench.simulator.utils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DeterministicCompletableFuture<T> extends CompletableFuture<T> {
    private static final Executor executor = Executors.newSingleThreadExecutor(
            runnable -> new Thread(runnable, "Custom-Single-Thread")
    );

    @Override
    public final Executor defaultExecutor() {
        return executor;
    }


}
