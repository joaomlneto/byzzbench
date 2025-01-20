package byzzbench.simulator.protocols.Zyzzyva;
import byzzbench.simulator.Client;
import byzzbench.simulator.Scenario;
import byzzbench.simulator.protocols.Zyzzyva.message.*;
import byzzbench.simulator.transport.DefaultClientReplyPayload;
import byzzbench.simulator.transport.MessagePayload;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;

import java.time.Duration;
import java.util.*;


/// TODO: POM of the Ordered requests
@Log
public class ZyzzyvaClient extends Client {

    @Getter
    private final int numFaults;

    @Getter
    private final int MAX_REQUESTS = 50;

    @Getter
    @Setter
    private int numRequests = 0;

    @Getter
    @Setter
    private long highestTimestamp;

    @Getter
    @Setter
    private long lapsedRequestTimeoutId;

    @Getter
    @Setter
    private RequestMessage lastRequest;

    @Getter
    @Setter
    private int viewNumber;

    @Getter
    // Request digest (using arrays.hashCode), List<SpeculativeResponseWrapper>
    // mustn't allow duplicates
    private final SortedMap<Integer, Set<SpeculativeResponseWrapper>> specResponses;

    @Getter
    // Request digest (using arrays.hashCode), Set<LocalCommitMessage>
    private final SortedMap<Integer, SortedSet<LocalCommitMessage>> localCommits;

    @Getter
    @Setter
    private boolean initialized = false;

    public ZyzzyvaClient(Scenario scenario, String id) {
        super(scenario, id);
        this.numFaults = (this.getScenario().getNodes().size() - 1) / 3;
        this.specResponses = new TreeMap<>();
        this.localCommits = new TreeMap<>();
        this.viewNumber = 0;
        this.highestTimestamp = -1L;
    }

    public void initialize() {
        if (this.initialized) {
            return;
        }
        this.clearAllTimeouts();
        this.sendRequest();
        this.initialized = true;
    }

    /**
     * Sends a request to the primary replica
     * Corresponds to A1 in the paper
     */
    @Override
    public void sendRequest() {
        this.clearAllTimeouts();
        String recipientId = this.computePrimaryId();
        String operation = String.format("%s/%d", this.getId(), this.getRequestSequenceNumber().getAndIncrement());
        /// TODO: look into using this.getCurrentTime()
        this.setHighestTimestamp(this.getHighestTimestamp() + 1);
        if (this.getNumRequests() == this.MAX_REQUESTS) {
            log.info("Client " + this.getId() + " has reached the maximum number of requests");
        }

        RequestMessage requestMessage = new RequestMessage(operation, this.getHighestTimestamp(), this.getId());
        this.getScenario().getTransport().sendMessage(this, requestMessage, recipientId);
        // transport timestamp alternative, doesn't work since the replica can't keep track of the timestamp
        // this.getScenario().getTransport().sendClientRequest(this.getId(), operation, recipientId);
        this.setLapsedRequestTimeoutId(this.setTimeout("lapsedRequestTimeout", this::lapsedRequest, Duration.ofMillis(15000)));
        this.setNumRequests(this.getNumRequests() + 1);
        if (this.getNumRequests() >= this.MAX_REQUESTS) {
            log.info("Client " + this.getId() + " has reached the maximum number of requests");
        }
        this.setLastRequest(requestMessage);
    }

    /**
     * Resends the request but to all replicas if less than 2f + 1 responses are received
     * Corresponds to A4c. in the paper
     */
    public void resendRequest() {
        SortedSet<String> recipientIds = (SortedSet<String>) this.getScenario().getReplicas().keySet();
        this.getScenario().getTransport().multicast(this, recipientIds, this.getLastRequest());
        this.setLapsedRequestTimeoutId(this.setTimeout("resendRequest", this::lapsedRequest, Duration.ofMillis(15000)));
    }

    @Override
    public void handleMessage(String senderId, MessagePayload message) {
        switch (message) {
            case DefaultClientReplyPayload defaultClientReplyPayload -> this.handleSpeculativeResponse((SpeculativeResponseWrapper) defaultClientReplyPayload.getReply());

            case SpeculativeResponseWrapper specResponseWrapper -> this.handleSpeculativeResponse(specResponseWrapper);

            case LocalCommitMessage localCommitMessage -> this.handleLocalCommit(localCommitMessage);

            default -> log.warning("Received unexpected message type: " + message.getType());
        }
    }

    public void handleSpeculativeResponse(SpeculativeResponseWrapper srw) {
        final byte[] lastDigest = this.digest(this.getLastRequest());
        final int lastDigestHash = Arrays.hashCode(lastDigest);
        // check if it corresponds to the last request
        if (Arrays.equals(srw.getOrderedRequest().getDigest(), lastDigest)) {

            this.getSpecResponses().putIfAbsent(lastDigestHash, new TreeSet<>());
            this.getSpecResponses().get(lastDigestHash).add(srw);

            // early termination condition
            if (this.numMatchingSpecResponses(this.getSpecResponses().get(lastDigestHash)) >= 3 * this.getNumFaults() + 1) {
                try {
                    this.clearTimeout(this.getLapsedRequestTimeoutId());
                } catch (IllegalArgumentException e) {
                    log.warning("Could not cancel the timeout: " + e.getMessage());
                }
                this.sendRequest();
            }
        } else {
            log.warning("Received a speculative response with an unexpected digest");
        }
    }

    public void handleLocalCommit (LocalCommitMessage localCommitMessage) {
        final int localCommitDigestHash = Arrays.hashCode(localCommitMessage.digest);
        // adds the local commit message
        this.getLocalCommits().putIfAbsent(localCommitDigestHash, new TreeSet<>());
        this.getLocalCommits().get(localCommitDigestHash).add(localCommitMessage);
    }

    /**
     * Client actions when the timer for the request has lapsed
     */
    public void lapsedRequest () {
        int numMatching = this.numMatchingSpecResponses(this.getSpecResponses().getOrDefault(this.getLastDigest(), new TreeSet<>()));
        if (numMatching >= 3 * this.getNumFaults() + 1) {
            this.sendRequest();
        } else if (2 * this.getNumFaults() + 1 <= numMatching && numMatching <= 3 * this.getNumFaults()) {
            this.sendCommitMessage();
        } else {
            this.resendRequest();
        }
    }

    /**
     * Sends a commit message to the replicas
     */
    public void sendCommitMessage() {
        log.info("Sending commit message");
        // Used for grouping SpeculativeResponseWrappers by SpeculativeResponse and finding the largest group
        HashMap<SpeculativeResponse, List<String>> specResponseToReplicaId = new HashMap<>();
        // group replicas by SpeculativeResponse
        for (SpeculativeResponseWrapper srw : this.getSpecResponses().get(this.getLastDigest())) {
            specResponseToReplicaId.putIfAbsent(srw.getSpecResponse(), new ArrayList<>());
            specResponseToReplicaId.get(srw.getSpecResponse()).add(srw.getReplicaId());
        }

        if (specResponseToReplicaId.isEmpty()) {
            log.warning("No SpeculativeResponseWrappers found for the last request");
            return;
        }

        Map.Entry<SpeculativeResponse, List<String>> largestEntry = specResponseToReplicaId.entrySet().stream()
                .max(Comparator.comparingInt(entry -> entry.getValue().size()))
                .orElse(null);

        List<String> signedBy = largestEntry.getValue();

        // Used to be thorough
        if (signedBy.size() != numMatchingSpecResponses(this.getSpecResponses().get(this.getLastDigest()))) {
            log.warning("Largest list size does not match the number of matching SpeculativeResponses");
        }

        SpeculativeResponse specResponse = largestEntry.getKey();

        // update view number if behind
        if (specResponse.getViewNumber() != this.getViewNumber()) this.setViewNumber((int) specResponse.getViewNumber());

        // create the commit message
        CommitMessage commitMessage = getCommitMessage(specResponse, signedBy);
        commitMessage.sign(this.getId());

        sendCommitUntilResponse(commitMessage);
    }

    private CommitMessage getCommitMessage(SpeculativeResponse specResponse, List<String> signedBy) {
        long sequenceNumber = specResponse.getSequenceNumber();
        long viewNumber = specResponse.getViewNumber();
        long history = specResponse.getHistory();

        /// TODO: ask about the signed responses (additional pedantic details 4b)
        CommitCertificate cc = new CommitCertificate(
                sequenceNumber,
                viewNumber,
                history,
                specResponse,
                signedBy
        );

        return new CommitMessage(this.getId(), cc);
    }

    /**
     * Sends the commit message until a response is received
     * @param commitMessage - the commit message to send
     */
    private void sendCommitUntilResponse(CommitMessage commitMessage) {
        SortedSet<String> recipientIds = (SortedSet<String>) this.getScenario().getReplicas().keySet();
        this.getScenario().getTransport().multicast(this, recipientIds, commitMessage);
        // Sets the timeout, clears the local commits if the number of local commits is greater than 2f
        this.setTimeout("commitTimeout", () -> {
            if (this.getLocalCommits().getOrDefault(this.getLastDigest(), new TreeSet<>()).size() >= 2 * this.getNumFaults() + 1) {
                this.getLocalCommits().clear();
                this.sendRequest();
            } else {
                this.sendCommitUntilResponse(commitMessage);
            }
        }, Duration.ofMillis(15000));
    }

    /**
     * Returns the highest number of matching SpecResponses taken from the SpecResponseWrappers.
     * The SpecResponseWrappers are all unique and don't allow for duplicates, however the SpecResponses are not unique and can be repeated
     * @param specResponseList - the set of SpecResponseWrappers
     * @return - the highest number of matching SpecResponses
     */
    public int numMatchingSpecResponses(Set<SpeculativeResponseWrapper> specResponseList) {
        HashMap<SpeculativeResponse, Integer> frequencies = new HashMap<>();
        for (SpeculativeResponseWrapper speculativeResponseWrapper : specResponseList) {
            SpeculativeResponse specResponse = speculativeResponseWrapper.getSpecResponse();
            frequencies.put(specResponse, frequencies.getOrDefault(specResponse, 0) + 1);
        }
        return frequencies.values().stream().max(Integer::compareTo).orElse(0);
    }

    private String computePrimaryId() {
        return this.getScenario().getReplicas().sequencedKeySet().toArray()[this.viewNumber % this.getScenario().getReplicas().size()].toString();
    }

    public int getLastDigest() {
        return Arrays.hashCode(this.digest(this.getLastRequest()));
    }
}
