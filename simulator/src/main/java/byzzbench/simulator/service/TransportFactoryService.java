package byzzbench.simulator.service;

import byzzbench.simulator.Replica;
import byzzbench.simulator.state.CommitLog;
import byzzbench.simulator.transport.Transport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.Serializable;

@Service
@RequiredArgsConstructor
public class TransportFactoryService {
    /**
     * The service for storing and managing schedules.
     */
    private final MessageMutatorService messageMutatorService;

    /**
     * The service for storing and managing schedules.
     */
    private final SchedulesService schedulesService;

    /**
     * Create a new transport instance.
     * @return The new transport instance.
     * @param <T> The type of the entries in the {@link CommitLog} of each {@link Replica}.
     */
    public <T extends Serializable> Transport<T> createTransport() {
        return new Transport<>(messageMutatorService, schedulesService);
    }
}
