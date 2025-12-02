package byzzbench.simulator.protocols.testconsensus;

import byzzbench.simulator.transport.MessagePayload;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Minimal message types for the TestConsensus protocol.
 */
public final class TestConsensusMessages {

    private TestConsensusMessages() {}

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Propose extends MessagePayload implements Serializable {
        private long slot; // sequence number
        private String value;

        @Override
        public String getType() {
            return "TESTCONSENSUS_PROPOSE";
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Ack extends MessagePayload implements Serializable {
        private long slot;
        private String value;

        @Override
        public String getType() {
            return "TESTCONSENSUS_ACK";
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Commit extends MessagePayload implements Serializable {
        private long slot;
        private String value;

        @Override
        public String getType() {
            return "TESTCONSENSUS_COMMIT";
        }
    }
}
