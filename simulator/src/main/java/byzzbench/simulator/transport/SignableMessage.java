package byzzbench.simulator.transport;

public abstract class SignableMessage implements MessagePayload {
    protected boolean signed;
    protected String signedBy;

    public void sign(String sender) {
        this.signed = true;
        this.signedBy = sender;
    }

    public boolean isSignedBy(String id) {
        return this.signed && this.signedBy.equals(id);
    }
}
