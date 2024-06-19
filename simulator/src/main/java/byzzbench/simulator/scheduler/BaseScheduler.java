package byzzbench.simulator.scheduler;

import byzzbench.simulator.transport.Event;
import byzzbench.simulator.transport.Transport;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.util.Optional;

@RequiredArgsConstructor
public abstract class BaseScheduler<T extends Serializable> {
    @Getter(AccessLevel.PROTECTED)
    private final Transport<T> transport;

    public abstract Optional<Event> scheduleNext() throws Exception;
}
