package byzzbench.simulator;

import byzzbench.simulator.config.ByzzBenchConfig;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.time.Instant;

@SpringBootApplication
@RequiredArgsConstructor
public class SimulatorApplication {
    @Getter
    private static Instant startTime;
    private final ByzzBenchConfig config;

    public static void main(String[] args) {
        SpringApplication.run(SimulatorApplication.class, args);
    }

    /**
     * Set the start time of the simulator.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        startTime = Instant.now();

        // print config
        System.out.println("Configuration: " + config);
    }
}
