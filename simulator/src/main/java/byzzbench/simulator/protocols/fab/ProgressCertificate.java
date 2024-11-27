package byzzbench.simulator.protocols.fab;

import java.util.Arrays;
import java.util.List;

public record ProgressCertificate(int proposalNumber, List<SignedResponse> responses) {

    // Ensure the certificate is valid and complete
    public boolean isValid(int quorumSize) {
        return responses.size() >= quorumSize;
    }

    // Check if the certificate vouches for a specific value
    public boolean vouchesFor(byte[] value) {
        long matchingResponses = responses.stream()
                .filter(response -> Arrays.equals(response.value(), value))
                .count();

        return matchingResponses >= (responses.size() / 2) + 1;
    }
}
