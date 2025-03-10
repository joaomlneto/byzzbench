package byzzbench.simulator.transport;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Event that represents a message being sent from one node to another.
 *
 * @see Event
 */
@EqualsAndHashCode(callSuper = true)
@Data
@JsonTypeName("Message")
@SuperBuilder
@ToString(callSuper = true)
public class MessageEvent extends BaseMessageEvent<MessagePayload> {
}
