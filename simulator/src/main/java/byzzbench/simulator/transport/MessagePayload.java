package byzzbench.simulator.transport;

import lombok.Getter;

import java.io.Serializable;

/**
 * Interface for the payload of a {@link MessageEvent}.
 */
@Getter
public abstract class MessagePayload implements Serializable {
  private boolean signed;
  private String signedBy;
  public abstract String getType();


  /*
  * Dummy function to validate the signature of the
  * message instance. Returns true if the message was
  * signed by @param id.
  */
  public boolean isSignedBy(String id) {
    return this.signed && this.signedBy.equals(id);
  }

  /*
  * Dummy function to sign the message with the public
  * key of the @param sender.
  */
  public void sign(String sender) {
    this.signed = true;
    this.signedBy = sender;
  }
}
