package bftbench.runner.pbft;

import lombok.Getter;

public class LinearBackoff {
    private final long initialTimeout;

    @Getter
    private long newViewNumber;

    @Getter
    private long timeout;

    private long startTime;

    @Getter
    private boolean waitingForVotes;

    public LinearBackoff(long curViewNumber, long timeout) {
        this.initialTimeout = timeout;
        this.newViewNumber = curViewNumber + 1;
        this.timeout = this.initialTimeout;
        this.startTime = System.currentTimeMillis();
    }

    public void expire() {
        this.newViewNumber++;
        this.timeout += this.initialTimeout;
        this.waitingForVotes = true;
    }

    public long elapsed() {
        return System.currentTimeMillis() - this.startTime;
    }

    public void beginNextTimer() {
        if (this.waitingForVotes) {
            this.waitingForVotes = false;
            this.startTime = System.currentTimeMillis();
        }
    }
}
