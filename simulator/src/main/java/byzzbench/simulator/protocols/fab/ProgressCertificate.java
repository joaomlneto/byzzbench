package byzzbench.simulator.protocols.fab;

import java.util.List;

public record ProgressCertificate(int proposalNumber, List<SignedResponse> responses) {

    // Ensure the certificate is valid and complete
    public boolean isValid(int quorumSize) {
        return responses.size() >= quorumSize;
    }

    // Check if the certificate vouches for a specific value
    public boolean vouchesFor(String value) {
        long matchingResponses = responses.stream()
                .filter(response -> response.value().equals(value))
                .count();

        return matchingResponses >= (responses.size() / 2) + 1;
    }
}
