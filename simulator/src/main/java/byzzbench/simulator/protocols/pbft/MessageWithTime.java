package byzzbench.simulator.protocols.pbft;

import java.time.Instant;
import java.util.Optional;

/**
 * An optional message with a timestamp.
 *
 * @param message the message
 * @param time    the timestamp
 * @param <T>     the type of the message
 */
public record MessageWithTime<T extends CertifiableMessage>(Optional<T> message,
                                                            Instant time) {}
