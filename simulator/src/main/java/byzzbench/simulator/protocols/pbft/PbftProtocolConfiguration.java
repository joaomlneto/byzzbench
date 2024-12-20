package byzzbench.simulator.protocols.pbft;

import lombok.Data;

/**
 * The configuration for the reference implementation of the PBFT protocol.
 * <p>
 * References:
 * <ol>
 *   <li>M. Castro and B. Liskov. Practical Byzantine Fault Tolerance (OSDI '99)</li>
 *   <li>M. Castro and B. Liskov. Proactive Recovery in a Byzantine-Fault-Tolerant System (OSDI '00)</li>
 *   <li>R. Rodrigues, M. Castro, and B. Liskov. BASE: Using Abstraction to Improve Fault Tolerance (SOSP '01)</li>
 * </ol>
 */
@Data
public class PbftProtocolConfiguration {
    /**
     * The type of protocol to use.
     */
    private final ProtocolType PROTOCOL_TYPE = ProtocolType.BFT;

    /**
     * The maximum number of replicas allowed in the system.
     * From parameters.h in the reference implementation.
     */
    private final int MAX_NUM_REPLICAS = 32;
    /**
     * Interval in sequence space between "checkpoint" states, i.e.
     * states that are checkpointed and for which Checkpoint messages are
     * sent.
     * From parameters.h in the reference implementation.
     */
    private final int CHECKPOINT_INTERVAL = 128;
    /**
     * Maximum number of messages for which protocol can be simultaneously
     * in progress, i.e., messages with sequence number
     * higher than last_stable+max_out are ignored. It is required that
     * max_out > checkpoint_interval. Otherwise, the algorithm will be
     * unable to make progress.
     */
    private final int MAX_OUT = 256;


    /**
     * BFT and BASE have different programming interfaces as described in [3].
     *
     * @return true if using BFT, false if using BASE
     */
    public boolean NO_STATE_TRANSLATION() {
        return PROTOCOL_TYPE == ProtocolType.BFT;
    }

    /**
     * BFT and BASE have different programming interfaces as described in [3].
     *
     * @return true if using BASE, false if using BFT
     */
    public boolean OBJ_REP() {
        return PROTOCOL_TYPE == ProtocolType.BASE;
    }

    public enum ProtocolType {
        BASE,
        BFT
    }
}
