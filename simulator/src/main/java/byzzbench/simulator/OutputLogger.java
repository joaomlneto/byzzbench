package byzzbench.simulator;

import byzzbench.simulator.faults.Fault;
import byzzbench.simulator.transport.Event;
import byzzbench.simulator.transport.MutateMessageEventPayload;
import byzzbench.simulator.transport.TimeoutEvent;
import byzzbench.simulator.transport.TransportObserver;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;

@Slf4j
public class OutputLogger implements TransportObserver {

    private final Path file;

    public OutputLogger(Path root, String scenarioId, int scenarioIndex) {
        file = root
                .resolve(scenarioId)
                .resolve(Instant.now().toString() + "_" + scenarioIndex + ".log");
        try {
            Files.createDirectories(file.getParent());
        } catch (IOException e) {
            log.error("Can't create output directory: {}", file.toAbsolutePath(), e);
        }
    }

    @Override
    public void onEventAdded(Event event) {
    }

    @Override
    public void onEventDropped(Event event) {
        appendLine("DROPPED: " + event);
    }

    @Override
    public void onEventRequeued(Event event) {

    }

    @Override
    public void onEventDelivered(Event event) {

    }

    @Override
    public void onMessageMutation(MutateMessageEventPayload payload) {
        appendLine("MUTATED: " + payload);
    }

    @Override
    public void onFault(Fault fault) {

    }

    @Override
    public void onTimeout(TimeoutEvent event) {

    }

    private void appendLine(String message) {
        try {
            Files.writeString(file, message + "\n", StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            log.error("error writing to file {}", file, e);
        }
    }
}
