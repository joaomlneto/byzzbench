package byzzbench.simulator;

import lombok.Getter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.time.Instant;

@SpringBootApplication
public class SimulatorApplication {
    @Getter
    private static Instant startTime;

    public static void main(String[] args) {
        SpringApplication.run(SimulatorApplication.class, args);
    }

    /**
     * Set the start time of the simulator.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        startTime = Instant.now();
    }
}
