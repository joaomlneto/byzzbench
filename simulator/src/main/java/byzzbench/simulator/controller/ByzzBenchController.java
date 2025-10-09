package byzzbench.simulator.controller;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.config.ByzzBenchConfig;
import byzzbench.simulator.domain.ScenarioParameters;
import byzzbench.simulator.faults.faults.MessageMutationFault;
import byzzbench.simulator.service.MessageMutatorService;
import byzzbench.simulator.service.ScenarioService;
import byzzbench.simulator.service.SimulatorService;
import byzzbench.simulator.utils.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Main controller for generic things
 */
@RestController
@RequiredArgsConstructor
public class ByzzBenchController {
    private final ByzzBenchConfig byzzBenchConfig;
    private final MessageMutatorService messageMutatorService;
    private final SimulatorService simulatorService;
    private final ScenarioService scenarioService;

    /**
     * Get the status of the simulator.
     *
     * @return The status of the simulator.
     */
    @GetMapping("/status")
    public String getStatus() {
        return "Running";
    }

    @GetMapping("/config")
    public ByzzBenchConfig getConfig() {
        return byzzBenchConfig;
    }

    /**
     * Get the list of saved schedules.
     *
     * @return The
     */
    @GetMapping("/saved-schedules")
    public SavedSchedulesInfo getAllScheduleIds() {
        List<Scenario> scenarios = scenarioService.getScenarios().values().stream().toList();
        synchronized (scenarios) {
            List<Integer> buggySchedules = scenarios.stream()
                    .filter(scenario -> scenario.getSchedule().isBuggy())
                    .map(scenarios::indexOf)
                    .toList();
            List<Integer> correctSchedules = scenarios.stream()
                    .filter(scenario -> !scenario.getSchedule().isBuggy())
                    .map(scenarios::indexOf)
                    .toList();
            return new SavedSchedulesInfo(
                    buggySchedules,
                    correctSchedules);
        }
    }

    /**
     * Generate a new scenario.
     */
    @PostMapping("/scenarios")
    public String createScenario() {
        // TODO accept parameters via POST body
        String scenarioId = simulatorService.getScenario().getDescription();
        this.simulatorService.changeScenario(scenarioId, new ScenarioParameters());
        return simulatorService.getScenario().getDescription();
    }

    /**
     * Get the mutator with the given ID.
     *
     * @param mutatorId The ID of the mutator to get.
     * @return The mutator with the given ID.
     */
    @GetMapping("/mutators/{mutatorId}")
    public MessageMutationFault getMutator(@PathVariable String mutatorId) {
        return messageMutatorService
                .getMutator(mutatorId);
    }


    /**
     * Get the list of enabled mutators for the scenario.
     * FIXME: this needs refactoring
     *
     * @return the set of mutators
     */
    @GetMapping("/mutators")
    public SortedSet<String> getMutators() {
        return messageMutatorService
                .getMutatorsMap()
                .keySet()
                .stream()
                .sorted()
                .collect(Collectors.toCollection(TreeSet::new));
    }

    public record SavedSchedulesInfo(@NonNull List<Integer> buggySchedules,
                                     @NonNull List<Integer> correctSchedules) {
    }
}
