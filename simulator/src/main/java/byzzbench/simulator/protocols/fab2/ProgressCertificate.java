package byzzbench.simulator.protocols.fab2;

import lombok.extern.java.Log;

import java.util.*;
import java.util.stream.Collectors;

@Log
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
        responses.values().stream()
                .map(SignedResponse::getValue)
                .filter(Objects::nonNull)
                .forEach(v -> valueCounts.merge(v, 1, Integer::sum));

        log.info("Value counts: " + valueCounts);
        valueCounts.forEach((k, v) -> log.info("Key: " + Arrays.toString(k) + ", Value: " + v));

        // Find the value that exceeds the threshold
        // Collect all entries meeting the threshold
        List<Map.Entry<byte[], Integer>> candidates = valueCounts.entrySet().stream()
                .filter(entry -> entry.getValue() >= threshold)
                .toList();

        // Ensure only one unique value meets or exceeds the threshold
        if (candidates.size() == 1) {
            return Optional.of(candidates.getFirst().getKey());
        } else if (candidates.isEmpty()) {
            return valueCounts.keySet().stream()
                    .findFirst();
        }
        else {
            log.info("No unique value meets the threshold.");
            return Optional.empty(); // Return empty if no unique value meets the criteria
        }
    }

    public boolean vouchesFor(byte[] value, int quorumSize) {
        // Count the number of responses that vouch for the value
        long count = responses.entrySet().stream()
                .filter(entry -> entry.getValue().getValue() != null && Arrays.equals(entry.getValue().getValue(), value))
                .count();

        // Check if the count exceeds the threshold
        return count >= quorumSize;
    }
}