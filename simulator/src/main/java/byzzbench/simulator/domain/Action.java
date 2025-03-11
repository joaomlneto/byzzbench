package byzzbench.simulator.domain;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.utils.serialization.ScheduleJsonConverter;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.function.Consumer;

@Entity
@DiscriminatorColumn(name = "action_type")
@Data
@SuperBuilder
@NoArgsConstructor
public abstract class Action implements Consumer<Scenario> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long actionId;

    @ManyToOne
    //@JoinColumn(name = "schedule_id")
    @Convert(converter = ScheduleJsonConverter.class)
    private Schedule schedule;
}
