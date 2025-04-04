package byzzbench.simulator.protocols.pbft_java;

import byzzbench.simulator.Timer;
import lombok.Getter;

import java.time.Duration;
import java.time.Instant;

public class LinearBackoff {
    /**
     * The replica object.
     */
    private final PbftJavaReplica<?, ?> replica;

    /**
     * The initial timeout for the linear backoff.
     */
    private final Duration initialTimeout;

    /**
     * The timer object for the linear backoff.
     */
    private final Timer timer;

    /**
     * The new view number.
     */
    @Getter
    private long newViewNumber;

    /**
     * The duration of the timeout.
     */
    @Getter
    private Duration timeout;

    /**
     * The start time of the linear backoff.
     */
    private Instant startTime;

    /**
     * Whether the linear backoff is waiting for votes.
     */
    @Getter
    private boolean waitingForVotes;

    public LinearBackoff(PbftJavaReplica<?, ?> replica, long curViewNumber, Duration timeout) {
        this.replica = replica;
        this.initialTimeout = timeout;
        this.newViewNumber = curViewNumber + 1;
        this.timeout = this.initialTimeout;
        this.startTime = replica.getCurrentTime();
        this.timer = new Timer(replica, "timeout", timeout, null);
    }

    public void expire() {
        this.newViewNumber++;
        this.timeout = this.timeout.plus(this.initialTimeout);
        this.waitingForVotes = true;
    }

    public Duration elapsed() {
        return Duration.between(this.startTime, this.replica.getCurrentTime());
    }

    public void beginNextTimer() {
        if (this.waitingForVotes) {
            this.waitingForVotes = false;
            this.startTime = this.replica.getCurrentTime();
        }
    }
}
