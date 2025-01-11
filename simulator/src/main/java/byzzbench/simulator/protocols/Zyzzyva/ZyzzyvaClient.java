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

@Log
public class ZyzzyvaClient extends Client {

    @Getter
    private final int numFaults;

    @Getter
    @Setter
    private long highestTimestamp;

    @Getter
    @Setter
    private long lapsedRequestTimeoutId;

    @Getter
    @Setter
    private long lastHistory;

    @Getter
    private final SortedMap<Long, List<SpeculativeResponseWrapper>> specResponses;

    @Getter
    private final SortedMap<Long, SortedSet<LocalCommitMessage>> localCommits;

    @Getter
    @Setter
    private boolean initialized = false;

    public ZyzzyvaClient(Scenario scenario, String id) {
        super(scenario, id);
        this.numFaults = (this.getScenario().getNodes().size() - 1) / 3;
        this.specResponses = new TreeMap<>();
        this.localCommits = new TreeMap<>();
        this.lastHistory = -1L;
    }

    public void initialize() {
        if (this.initialized) {
            return;
        }
        this.clearAllTimeouts();
        this.sendRequest();
        this.initialized = true;
    }

    @Override
    public void sendRequest() {
        this.clearAllTimeouts();
        String recipientId = this.getScenario().getReplicas().keySet().iterator().next();
        String operation = String.format("%s/%d", this.getId(), this.getRequestSequenceNumber().getAndIncrement());
        this.setHighestTimestamp(this.getHighestTimestamp() + 1);

        RequestMessage requestMessage = new RequestMessage(operation, this.getHighestTimestamp(), this.getId());
        this.getScenario().getTransport().sendMessage(this, requestMessage, recipientId);
        // transport timestamp alternative, doesn't work since the replica can't keep track of the timestamp
//        this.getScenario().getTransport().sendClientRequest(this.getId(), operation, recipientId);
        this.setTimeout("lapsedRequestTimeout", this::lapsedRequest, Duration.ofMillis(15000));
    }

    public void resendRequest() {
        SortedSet<String> recipientIds = (SortedSet<String>) this.getScenario().getReplicas().keySet();
        String operation = String.format("%s/%d", this.getId(), this.getRequestSequenceNumber().get());
        RequestMessage requestMessage = new RequestMessage(operation, this.getHighestTimestamp(), this.getId());

        this.getScenario().getTransport().multicast(this, recipientIds, requestMessage);
        /// TODO: See what happens here
        this.setTimeout("resendRequest", () -> {log.warning("Liveness issue");}, Duration.ofMillis(15000));
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
        if (srw.getSpecResponse().getTimestamp() == this.getHighestTimestamp()) {
            if (!this.getSpecResponses().containsKey(this.getHighestTimestamp())) {
                this.getSpecResponses().put(this.getHighestTimestamp(), new ArrayList<>());
            }
            this.getSpecResponses().get(this.getHighestTimestamp()).add(srw);

            // early termination condition
            if (this.numMatchingSpecResponses(this.getSpecResponses().get(this.getHighestTimestamp())) >= 3 * this.getNumFaults() + 1) {
                try {
                    this.clearTimeout(this.getLapsedRequestTimeoutId());
                    this.sendRequest();
                } catch (IllegalArgumentException e) {
                    log.warning("Could not cancel the timeout: " + e.getMessage());
                }
                this.sendRequest();
            }
        }
        else {
            log.warning("Received a speculative response with an unexpected timestamp");
        }
    }

    public void handleLocalCommit (LocalCommitMessage localCommitMessage) {
        // adds the local commit message
        if (!this.getLocalCommits().containsKey(localCommitMessage.history)) this.getLocalCommits().put(localCommitMessage.history, new TreeSet<>());
        this.getLocalCommits().get(localCommitMessage.history).add(localCommitMessage);
        this.setLastHistory(localCommitMessage.history);
    }

    public void lapsedRequest () {
        System.out.println("Lapsed request timeout");
        int numMatching = this.numMatchingSpecResponses(this.getSpecResponses().getOrDefault(this.getHighestTimestamp(), new ArrayList<>()));

        if (numMatching >= 3 * this.getNumFaults() + 1) {
            this.updateLastHistory();
            this.sendRequest();
        } else if (2 * this.getNumFaults() + 1 <= numMatching && numMatching <= 3 * this.getNumFaults()) {
            this.sendCommitMessage();
        }
        else {
            this.resendRequest();
        }
    }

    public void sendCommitMessage() {

        HashMap<SpeculativeResponse, List<SpeculativeResponseWrapper>> specResponses = new HashMap<>();
        for (SpeculativeResponseWrapper srw : this.getSpecResponses().get(this.getHighestTimestamp())) {
            specResponses.put(srw.getSpecResponse(), specResponses.getOrDefault(srw.getSpecResponse(), new ArrayList<>()));
            specResponses.get(srw.getSpecResponse()).add(srw);
        }
        // find the largest arraylist in specResponses
        List<SpeculativeResponseWrapper> largestList = specResponses.values().stream()
                .max(Comparator.comparingInt(List::size))
                .orElse(new ArrayList<>());

        SpeculativeResponse specResponse = largestList.getFirst().getSpecResponse();

        TreeMap<String, SpeculativeResponse> signedResponses = new TreeMap<>();

        for (SpeculativeResponseWrapper srw : largestList) {
            signedResponses.put(srw.getReplicaId(), srw.getSpecResponse());
        }

        CommitMessage commitMessage = getCommitMessage(specResponse, largestList, signedResponses);

        sendCommitUntilResponse(commitMessage);
    }

    private CommitMessage getCommitMessage(SpeculativeResponse specResponse, List<SpeculativeResponseWrapper> largestList, TreeMap<String, SpeculativeResponse> signedResponses) {
        long sequenceNumber = specResponse.getSequenceNumber();
        long viewNumber = specResponse.getViewNumber();
        long timestamp = specResponse.getTimestamp();
        byte[] digest = largestList.getFirst().getOrderedRequest().getDigest();

        CommitCertificate cc = new CommitCertificate(
                sequenceNumber,
                viewNumber,
                timestamp,
                digest,
                signedResponses,
                this.getId()
        );

        return new CommitMessage(this.getId(), cc);
    }

    private void sendCommitUntilResponse(CommitMessage commitMessage) {
        SortedSet<String> recipientIds = (SortedSet<String>) this.getScenario().getReplicas().keySet();
        this.getScenario().getTransport().multicast(this, recipientIds, commitMessage);

        this.setTimeout("commitTimeout", () -> {
            System.out.println("Commit timeout");
            if (this.getLocalCommits().getOrDefault(this.getLastHistory(), new TreeSet<>()).size() > 2 * this.getNumFaults()) {
                this.getLocalCommits().clear();
                this.updateLastHistory();
                this.sendRequest();
            } else {
                this.sendCommitUntilResponse(commitMessage);
            }
        }, Duration.ofMillis(15000));
    }

    private void updateLastHistory() {
        this.setLastHistory(this.getSpecResponses().getOrDefault(this.getHighestTimestamp(), new ArrayList<>()).getFirst().getSpecResponse().getHistory());
    }

    public int numMatchingSpecResponses(List<SpeculativeResponseWrapper> specResponseList) {
        HashMap<SpeculativeResponse, Integer> frequencies = new HashMap<>();
        for (SpeculativeResponseWrapper speculativeResponseWrapper : specResponseList) {
            SpeculativeResponse specResponse = speculativeResponseWrapper.getSpecResponse();
            frequencies.put(specResponse, frequencies.getOrDefault(specResponse, 0) + 1);
        }
        return frequencies.values().stream().max(Integer::compareTo).orElse(0);
    }
}
