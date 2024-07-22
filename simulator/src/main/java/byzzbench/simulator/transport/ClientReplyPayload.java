package byzzbench.simulator.transport;

import java.io.Serializable;

public record ClientReplyPayload(String clientId, Serializable reply)
    implements Serializable {}
