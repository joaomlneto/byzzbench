package byzzbench.simulator.domain;

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
    private String scenarioFactoryId;

    /**
     * The unique identifier of the exploration strategy used to generate scenarios for this campaign.
     */
    private String explorationStrategyId;

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
     * Get the list of schedule IDs for this campaign.
     *
     * @return a list of schedule IDs
     */
    public @NonNull List<Long> getScheduleIds() {
        return schedules.stream().map(Schedule::getScheduleId).toList();
    }
}
