package byzzbench.simulator.domain;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.utils.NonNull;
import byzzbench.simulator.utils.serialization.ScheduleJsonConverter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.function.Consumer;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "action_type")
@Data
@SuperBuilder
@NoArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = DeliverMessageAction.class, name = "DeliverMessageAction"),
        @JsonSubTypes.Type(value = FaultInjectionAction.class, name = "FaultInjectionAction"),
        @JsonSubTypes.Type(value = TriggerTimeoutAction.class, name = "TriggerTimeoutAction"),
})
public abstract class Action implements Consumer<Scenario> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @NonNull
    private long actionId;

    @ManyToOne
    //@JoinColumn(name = "schedule_id")
    @Convert(converter = ScheduleJsonConverter.class)
    @JsonIgnore
    private Schedule schedule;
}
