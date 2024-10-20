package byzzbench.simulator;

import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A timekeeper that provides timestamps to the replicas in the simulator.
 */
@RequiredArgsConstructor
public class Timekeeper {
    private final Scenario scenario;
    private final AtomicLong counter = new AtomicLong(0);

    public Instant getTime(Replica replica) {
        return Instant.ofEpochMilli(counter.incrementAndGet());
    }
}
