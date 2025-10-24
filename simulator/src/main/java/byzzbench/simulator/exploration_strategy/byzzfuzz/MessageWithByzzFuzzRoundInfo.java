package byzzbench.simulator.exploration_strategy.byzzfuzz;

import byzzbench.simulator.transport.MessagePayload;

/**
 * Interface for {@link MessagePayload} that include a round number as per the
 * <a href="https://dl.acm.org/doi/10.1145/3586053">ByzzFuzz algorithm</a>
 */
public interface MessageWithByzzFuzzRoundInfo {
    /**
     * Get the view number in the message
     *
     * @return The view number
     */
    long getViewNumber();

    /**
     * Get the round number of the message
     *
     * @return The round number
     */
    long getRound();
}
