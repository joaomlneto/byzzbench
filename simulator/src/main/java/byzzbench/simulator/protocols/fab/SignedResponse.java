package byzzbench.simulator.protocols.fab;

import java.util.Objects;

public record SignedResponse(String value, int proposalNumber, String signature) {
    // Simple signature verification method
    public boolean isSignatureValid(String publicKey) {
        return Objects.hash(value, proposalNumber) == Integer.parseInt(signature);
    }
}

