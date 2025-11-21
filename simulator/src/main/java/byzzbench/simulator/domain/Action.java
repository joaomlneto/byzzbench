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
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
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
        @JsonSubTypes.Type(value = FaultInjectionAction.class, name = "CorruptInFlightMessageAction"),
})
@ToString
public abstract class Action implements Consumer<Scenario>, Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @NonNull
    @JsonIgnore // this only makes sense in the database context. Don't send it to the client.
    private long actionId;

    @ManyToOne
    //@JoinColumn(name = "schedule_id")
    @Convert(converter = ScheduleJsonConverter.class)
    @JsonIgnore
    @ToString.Exclude
    private Schedule schedule;

    /**
     * Applies the specific (deterministic) action to the scenario
     *
     * @param scenario the scenario
     */
    @Override
    public abstract void accept(Scenario scenario);
}
