package byzzbench.simulator.protocols.pbft;

import java.time.Instant;

public class Timer {
    /**
     * The parent replica object.
     */
    private final PbftReplica replica;

    /**
     * The accumulated time.
     */
    private final double accumulated = 0.0;

    /**
     * True if the timer is running.
     */
    private boolean running = false;

    /**
     * The time when the timer was started.
     */
    private Instant start_time;

    public Timer(PbftReplica replica) {
        this.replica = replica;
    }

    /**
     * Starts the timer.
     */
    public void reset() {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void start() {
        if (!running) {
            this.running = true;
            //this.start_time = Instant.now(); // FIXME!!
            throw new UnsupportedOperationException("Not implemented");
        }
        throw new UnsupportedOperationException("Not implemented");
    }

    public void stop() {
        if (running) {
            this.running = false;
            //this.accumulated += diff_time(this.start_time, Instant.now()); // FIXME!!
            throw new UnsupportedOperationException("Not implemented");
        }
        throw new UnsupportedOperationException("Not implemented");
    }

    public void restop() {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void restart() {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Check how much time has elapsed since the timer was created or last reset.
     *
     * @return the elapsed time in seconds
     */
    public double elapsed() {
        if (running) {
            //return this.accumulated + diff_time(this.start_time, Instant.now()); // FIXME!!
            //throw new UnsupportedOperationException("Not implemented!!");
        } else {
            //return this.accumulated;
        }
        throw new UnsupportedOperationException("Not implemented!!");
    }
}
