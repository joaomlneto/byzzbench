package byzzbench.simulator.transport;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Interface for the payload of a {@link MessageEvent}.
 */
@Getter
@NoArgsConstructor
@Data
public abstract class MessagePayload implements Serializable {
    private String signedBy;

    /**
     * A string representation of the message type.
     */
    public abstract String getType();


    /*
     * Dummy function to validate the signature of the
     * message instance. Returns true if the message was
     * signed by @param id.
     */
    public boolean isSignedBy(String id) {
        return this.isSigned() && this.signedBy.equals(id);
    }

    /*
     * Dummy function to sign the message with the public
     * key of the @param sender.
     */
    public void sign(String sender) {
        this.signedBy = sender;
    }

    public boolean isSigned() {
        return this.signedBy != null;
    }
}
