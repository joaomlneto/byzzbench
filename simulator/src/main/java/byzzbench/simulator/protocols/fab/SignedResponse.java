package byzzbench.simulator.protocols.fab;

import java.util.Arrays;
import java.util.Objects;

public record SignedResponse(byte[] value, int proposalNumber, String signature) {
    // Simple signature verification method
    public boolean isSignatureValid(String publicKey) {
        return Objects.hash(Arrays.hashCode(value), proposalNumber) == Integer.parseInt(signature);
    }
}

