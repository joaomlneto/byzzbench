package byzzbench.simulator.domain;

import byzzbench.simulator.config.CampaignConfig;
import byzzbench.simulator.config.TerminationConfig;
import byzzbench.simulator.exploration_strategy.ExplorationStrategy;
import byzzbench.simulator.exploration_strategy.ExplorationStrategyParameters;
import byzzbench.simulator.service.ApplicationContextProvider;
import byzzbench.simulator.service.CampaignService;
import byzzbench.simulator.service.ExplorationStrategyService;
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
     * The parameters for the exploration strategy
     */
    @Column(length = 2048)
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
     * How many schedules were aborted due to correctness violations (invariant violations)
     */
    private long numTerm = 0;

    /**
     * How many schedules were aborted due to execution errors (implementation issues)
     */
    private long numErr = 0;

    /**
     * How many schedules were finished due to max length (OK)
     */
    private long numMaxedOut = 0;

    /**
     * The exploration strategy instance for this campaign
     */
    @Transient
    @JsonIgnore
    private transient ExplorationStrategy explorationStrategy;

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
        campaign.setNumScenarios(config.getNumScenarios());
        campaign.setRandom(new Random(config.getInitialRandomSeed()));
        campaign.setTermination(config.getTermination());
        campaign.setExplorationStrategyParameters(config.getExplorationStrategyParameters());
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

    /**
     * Process the result of a given scenario
     *
     * @param result the result
     */
    public void processScenarioResult(CampaignService.ScenarioExecutionResult result) {
        switch (result) {
            case CampaignService.ScenarioExecutionResult.CORRECT -> numMaxedOut++;
            case CampaignService.ScenarioExecutionResult.TERMINATED -> numTerm++;
            case CampaignService.ScenarioExecutionResult.ERRORED -> numErr++;
        }
    }

    /**
     * The unique name for the exploration strategy instance for this campaign
     *
     * @return the unique identifier for the exploration strategy instance
     */
    public String getExplorationStrategyInstanceId() {
        return "campaign-" + getCampaignId();
    }

    /**
     * Retrieve the exploration strategy used for deciding actions on scenarios in the campaign
     *
     * @return the exploration strategy instance
     */
    public ExplorationStrategy getExplorationStrategy() {
        if (this.explorationStrategy == null) {
            ExplorationStrategyService explorationStrategyService = ApplicationContextProvider.getExplorationStrategyService();
            this.explorationStrategy = explorationStrategyService.createExplorationStrategy(this);
        }
        return this.explorationStrategy;
    }
}
