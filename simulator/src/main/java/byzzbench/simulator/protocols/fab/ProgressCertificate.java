package byzzbench.simulator.protocols.fab;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public record ProgressCertificate(long proposalNumber, SortedMap<String, SignedResponse> responses) {
    // Ensure the certificate is valid and complete
    public boolean isValid(int quorumSize) {
        return responses.size() >= quorumSize;
    }

    // We say that a progress certificate ((v0, pn), ..., (va− f , pn)) vouches for the pair (v, pn)
    // if there is no value vi ̸= v that appears ⌈(a− f + 1)/2⌉ times in the progress certificate.
    public Optional<byte[]> majorityValue(int threshold) {
        // Transform the responses into a map of values and their counts
        Map<byte[], Integer> valueCounts = new HashMap<>();
        responses.entrySet().stream().map(entry -> entry.getValue().getValue()).forEach(v -> valueCounts.merge(v, 1, Integer::sum));

        // Find the value that exceeds the threshold
        return valueCounts.entrySet().stream()
                .filter(entry -> entry.getValue() > threshold)
                .map(Map.Entry::getKey)
                .findFirst();
    }

    public boolean vouchesFor(byte[] value, int quorumSize) {
        // Count the number of responses that vouch for the value
        long count = responses.entrySet().stream()
                .filter(entry -> Arrays.equals(entry.getValue().getValue(), value))
                .count();

        // Check if the count exceeds the threshold
        return count >= quorumSize;
    }
}