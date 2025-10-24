package byzzbench.simulator.domain;

import byzzbench.simulator.config.CampaignConfig;
import byzzbench.simulator.config.TerminationConfig;
import byzzbench.simulator.exploration_strategy.ExplorationStrategyParameters;
import byzzbench.simulator.utils.NonNull;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A collection of schedules generated using a specific strategy and a specific set of parameters.
 */
@Entity
@NoArgsConstructor
@Data
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class Campaign implements Serializable {
    /**
     * The initial random seed used to generate the scenarios for this campaign.
     */
    private long initialRandomSeed;

    /**
     * The unique identifier of the scenario factory used to generate scenarios for this campaign.
     */
    private String scenarioId;

    /**
     * The unique identifier of the exploration strategy used to generate scenarios for this campaign.
     */
    private String explorationStrategyId;

    /**
     * The parameters for the exploration strategy
     */
    private ExplorationStrategyParameters explorationStrategyParameters;

    /**
     * A random number generator initialized with the initial random seed.
     */
    @JsonIgnore
    private Random random;

    /**
     * The unique identifier for this campaign.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long campaignId;

    /**
     * The target number of scenarios that have been generated for this campaign.
     */
    private long numScenarios;

    /**
     * The schedules saved for this campaign.
     */
    @OneToMany(mappedBy = "campaign", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonIgnore
    private List<Schedule> schedules = new ArrayList<>();

    /**
     * When this campaign was created.
     */
    @CreatedDate
    @NonNull
    private Instant createdAt = Instant.now();

    /**
     * The termination configuration for this campaign.
     */
    @Embedded
    private TerminationConfig termination;

    /**
     * Create a campaign from a campaign configuration.
     *
     * @param config the campaign configuration
     * @return the created campaign
     */
    public static Campaign fromConfig(CampaignConfig config) {
        // ensure user is not trying to override scenario parameters that are set by the campaign
        if (config.getScenarioParameters().getRandomSeed() != null) {
            throw new IllegalArgumentException("Scenario parameters in campaign config must not have a random seed set");
        }

        Campaign campaign = new Campaign();
        campaign.setInitialRandomSeed(config.getInitialRandomSeed());
        campaign.setScenarioId(config.getScenarioParameters().getScenarioId());
        campaign.setExplorationStrategyId(config.getExplorationStrategyId());
        campaign.setNumScenarios(config.getNumScenarios());
        campaign.setRandom(new Random(config.getInitialRandomSeed()));
        campaign.setTermination(config.getTermination());
        return campaign;
    }

    /**
     * Get the list of schedule IDs for this campaign.
     *
     * @return a list of schedule IDs
     */
    public @NonNull List<Long> getScheduleIds() {
        return schedules.stream().map(Schedule::getScheduleId).toList();
    }

    /**
     * Generate the parameters for the next scenario in this campaign.
     *
     * @return the parameters for the next scenario
     */
    public ScenarioParameters getScenarioParameters() {
        return ScenarioParameters.builder()
                .randomSeed(this.random.nextLong())
                .scenarioId(this.scenarioId)
                // TODO set other parameters from campaign configuration
                .build();
    }
}
