package byzzbench.simulator.transport;

import byzzbench.simulator.faults.Fault;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Event that represents a message mutation.
 *
 * @see Action
 */
@EqualsAndHashCode(callSuper = true)
@Data
@JsonTypeName("GenericFault")
@SuperBuilder
@ToString(callSuper = true)
public class GenericFaultAction extends Action {
    /**
     * The payload of the request.
     */
    private final Fault payload;
}
