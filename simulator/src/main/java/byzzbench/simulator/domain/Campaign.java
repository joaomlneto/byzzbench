package byzzbench.simulator.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * A collection of schedules generated using a specific strategy and a specific set of parameters.
 */
@Entity
@NoArgsConstructor
@Data
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class Campaign {
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
     * Get the list of schedule IDs for this campaign.
     *
     * @return a list of schedule IDs
     */
    public List<Long> getScheduleIds() {
        return schedules.stream().map(Schedule::getScheduleId).toList();
    }
}
