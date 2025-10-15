package byzzbench.simulator.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

import java.io.Serializable;
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
    @Builder.Default
    private Long randomSeed = 1L;

    /**
     * Number of clients in the scenario.
     */
    @Builder.Default
    private Integer numClients = 1;

    /**
     * Number of replicas in the scenario.
     */
    private Integer numReplicas;

    /**
     * List of faults to be injected in the scenario.
     */
    private List<String> faults;

    /**
     * Checks if the scenario parameters are valid.
     *
     * @return true if the scenario parameters are valid, false otherwise.
     */
    @JsonIgnore
    public boolean isValid() {
        return scenarioId != null && !scenarioId.isEmpty() && randomSeed != null;
    }
}
