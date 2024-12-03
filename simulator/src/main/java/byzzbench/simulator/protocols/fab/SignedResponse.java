package byzzbench.simulator.protocols.fab;

import lombok.Data;
import lombok.Getter;

@Data
@Getter
public class SignedResponse {
    private final byte[] value;
    private final long proposalNumber;
    private final boolean isSigned;
    private final String sender;

    public SignedResponse(byte[] value, long proposalNumber, boolean isSigned, String sender) {
        this.value = value;
        this.proposalNumber = proposalNumber;
        this.isSigned = isSigned;
        this.sender = sender;
    }

    // Simple signature verification method
    public boolean isSignatureValid(String publicKey) {
       return isSigned;
    }
}

