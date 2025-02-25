package byzzbench.simulator;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.extern.java.Log;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;

@Log
public class Timer implements Serializable {
    /**
     * The parent replica object.
     */
    @JsonIgnore
    private transient final Replica replica;
    /**
     * The name of the timer.
     */
    private final String name;
    /**
     * The timeout duration.
     */
    @Getter
    private final Duration timeout;
    /**
     * The function to run when the timer expires.
     */
    @Getter
    @JsonIgnore
    private final transient Runnable callback;
    /**
     * The unique identifier of the ByzzBench event associated with this timer.
     */
    private Long eventId;
    /**
     * True if the timer is running.
     */
    @Getter
    private boolean running = false;

    /**
     * The time when the timer was started.
     */
    @Getter
    private Instant start_time;

    /**
     * Creates a new timer for the given replica with the given timeout.
     *
     * @param replica  the parent replica
     * @param timeout  the timeout in seconds
     * @param callback the function to run when the timer expires
     */
    public Timer(Replica replica, String name, Duration timeout, Runnable callback) {
        this.replica = replica;
        this.name = name;
        this.timeout = timeout;
        this.callback = callback;
    }

    /**
     * Start the timer.
     */
    public void start() {
        if (!running) {
            this.running = true;
            this.eventId = this.replica.setTimeout(this.name, this.callback, this.timeout);
            this.start_time = this.replica.getCurrentTime();
        } else {
            log.warning(String.format("Cannot start Timer %s: it's already running!", this.name));
        }
    }

    /**
     * Restart the timer.
     */
    public void restart() {
        if (!running) {
            log.warning(String.format("Cannot restart Timer %s: it's not running!", this.name));
        }

        this.stop();
        this.start();
    }

    /**
     * Stop the timer.
     */
    public void stop() {
        if (running) {
            this.running = false;
            this.replica.clearTimeout(this.eventId);
            this.start_time = null;
            this.eventId = null;
        } else {
            log.warning(String.format("Cannot stop Timer %s: it's not running!", this.name));
        }
    }

    /**
     * Stop the timer, and force setting its state to Stopped.
     */
    public void restop() {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Get the elapsed time since the timer was started.
     *
     * @return the elapsed time
     */
    public Duration elapsed() {
        if (running) {
            return Duration.between(start_time, replica.getCurrentTime());
        } else {
            return Duration.ZERO;
        }
    }

}
