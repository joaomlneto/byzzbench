package byzzbench.simulator.transport;

public abstract class SignableMessage implements MessagePayload {
    /*
     * Super-class for messages that can be signed and validated
     * Currently the signatures don't implement any cryptography and
     * are just dummy functions.
     */

    protected boolean signed;
    protected String signedBy;

    /*
     * Dummy function to sign the message with the public
     * key of the @param sender.
     */
    public void sign(String sender) {
        this.signed = true;
        this.signedBy = sender;
    }

    /*
     * Dummy function to validate the signature of the
     * message instance. Returns true if the message was
     * signed by @param id.
     */
    public boolean isSignedBy(String id) {
        return this.signed && this.signedBy.equals(id);
    }
}
