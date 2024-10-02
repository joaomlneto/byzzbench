package byzzbench.simulator.transport;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * Event that represents a request from a client to a node.
 *
 * @see Event
 */
@Data
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("ClientReply")
@SuperBuilder
public class ClientReplyEvent extends BaseMessageEvent<Serializable> {
}
