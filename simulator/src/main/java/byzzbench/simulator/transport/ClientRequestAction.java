package byzzbench.simulator.transport;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Event that represents a request from a client to a node.
 *
 * @see Action
 */
@EqualsAndHashCode(callSuper = true)
@Data
@JsonTypeName("ClientRequest")
@SuperBuilder
@ToString(callSuper = true)
public class ClientRequestAction extends BaseMessageAction<MessagePayload> {
}
