package byzzbench.simulator.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Base class for scenario parameters: the input to a scenario that defines its configuration.
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Jacksonized
@SuperBuilder
public class ScenarioParameters implements Serializable {
    /**
     * The unique identifier of the scenario.
     */
    private String scenarioId;

    /**
     * The random seed used to generate the scenario.
     */
    private Long randomSeed;

    /**
     * Number of clients in the scenario.
     */
    private Integer numClients;

    /**
     * Number of replicas in the scenario.
     */
    private Integer numReplicas;

    /**
     * List of faults to be injected in the scenario.
     */
    @Builder.Default
    private List<String> faults = new ArrayList<>();
}
