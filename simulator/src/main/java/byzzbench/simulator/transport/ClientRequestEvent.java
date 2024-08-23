package byzzbench.simulator.transport;

import com.fasterxml.jackson.annotation.JsonTypeName;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

/**
 * Event that represents a request from a client to a node.
 *
 * @see Event
 */
@EqualsAndHashCode(callSuper = true)
@Data
@JsonTypeName("ClientRequest")
@SuperBuilder
public class ClientRequestEvent extends BaseMessageEvent<Serializable> {}
