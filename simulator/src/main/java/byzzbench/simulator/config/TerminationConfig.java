package byzzbench.simulator.config;

import lombok.Data;

import java.io.Serializable;

/**
 * Configuration for the termination
 */
@Data
public class TerminationConfig implements Serializable {
    /**
     * The minimum number of events to run before termination. 0 means no minimum.
     */
    private long minEvents = 0;
    /**
     * The minimum number of rounds to run before termination. 0 means no minimum.
     */
    private long minRounds = 2;
    /**
     * Frequency of checking termination conditions.
     * Setting it to "1" means check every round, 2 means check every other round, etc.
     * The default is 1 (check every round).
     */
    private long samplingFrequency = 1;
}
