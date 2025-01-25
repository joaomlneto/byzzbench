package byzzbench.simulator.protocols.Zyzzyva;

import byzzbench.simulator.LeaderBasedProtocolReplica;
import byzzbench.simulator.Scenario;
import byzzbench.simulator.protocols.Zyzzyva.message.*;
import byzzbench.simulator.state.SerializableLogEntry;
import byzzbench.simulator.state.TotalOrderCommitLog;
import byzzbench.simulator.transport.DefaultClientRequestPayload;
import byzzbench.simulator.transport.MessagePayload;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.java.Log;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.io.Serializable;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Getter
@Log
@ToString(callSuper = true)
public class ZyzzyvaReplica extends LeaderBasedProtocolReplica {
    private final int CP_INTERVAL;

    private final int faultsTolerated;

    @Setter
    private long highestTimestamp = -1;

    @Setter
    private long highestSequenceNumber = 0;

    @Setter
    private boolean disgruntled = false;

    private final SortedSet<String> nodeIds;

    // used for the speculative history (along with the ordered requests in the messagelog)
    private final SpeculativeHistory history;

    @Setter
    private long requestTimeoutId = -1;

    @Setter
    private long forwardToPrimaryTimeoutId = -1;

    @Setter
    private long fillHoleTimeoutId = -1;

    @Setter
    private long fillHoleTimeoutAllId = -1;

    @Setter
    private long fillHoleMin = -1;

    @Setter
    private long fillHoleMax = -1;

    @Setter
    private boolean fillHoleActive = false;

    @JsonIgnore
    private final MessageLog messageLog;

    public ZyzzyvaReplica(String replicaId,
                          SortedSet<String> nodeIds,
                          Scenario scenario,
                          int CP_INTERVAL) {
        super(replicaId, scenario, new TotalOrderCommitLog());
        this.nodeIds = nodeIds;

        this.history = new SpeculativeHistory();
        this.messageLog = new MessageLog();
        this.CP_INTERVAL = CP_INTERVAL;
        this.faultsTolerated = (nodeIds.size() - 1) / 3;

        CommitCertificate startCC = new CommitCertificate(
                0,
                0,
                -1,
                new byte[0],
                new SpeculativeResponse(0, 0, -1, new byte[0], "", -1),
                new ArrayList<>()
        );
        // we set this as a null commit certificate for view changes etc. this is the first instance the system is stable
        this.getMessageLog().setMaxCC(startCC);

//        this.setRequestTimeoutId(this.setTimeout(
//                "request",
//                () -> {
//                    log.warning("Request timeout triggered");
//                    IHateThePrimaryMessage ihtpm = new IHateThePrimaryMessage(this.getViewNumber());
//                    ihtpm.sign(this.getId());
//                    this.broadcastMessage(ihtpm);
//                    this.handleIHateThePrimaryMessage(this.getId(), ihtpm);
//                },
//                Duration.ofMillis(30000)
//        ));
    }

    @Override
    public void initialize() {
        this.setView(0);
    }

    @Override
    public void handleMessage(String sender, MessagePayload m) {
        switch (m) {
            case DefaultClientRequestPayload defaultClientRequestPayload -> {
                if (this.disgruntled) {
                    return;
                }
                RequestMessage requestMessage = new RequestMessage(
                        defaultClientRequestPayload.getOperation(),
                        defaultClientRequestPayload.getTimestamp(),
                        sender);
                this.handleMessage(sender, requestMessage);
            }

            case RequestMessage requestMessage -> {
                if (this.disgruntled) {
                    return;
                }
                handleClientRequest(sender, requestMessage);
            }

            case OrderedRequestMessageWrapper orderedRequestMessage -> {
                if (this.disgruntled) {
                    return;
                }
                handleOrderedRequestMessageWrapper(sender, orderedRequestMessage);
            }

            case FillHoleMessage fillHoleMessage -> {
                if (this.disgruntled) {
                    return;
                }
                handleFillHoleMessageRequest(sender, fillHoleMessage);
            }

            case FillHoleReply fillHoleReply -> handleFillHoleReply(sender, fillHoleReply);

            case CommitMessage commitmessage -> {
                if (this.disgruntled) {
                    return;
                }
                handleCommitMessage(sender, commitmessage);
            }

            case IHateThePrimaryMessage iHateThePrimaryMessage -> {
                if (this.disgruntled) {
                    return;
                }
                handleIHateThePrimaryMessage(sender, iHateThePrimaryMessage);
            }

            case ViewChangeMessageWrapper viewChangeMessageWrapper ->
                    handleViewChangeMessageWrapper(sender, viewChangeMessageWrapper);

            case ProofOfMisbehaviorMessage proofOfMisbehaviorMessage -> {
                    if (this.disgruntled) {
                        return;
                    }
                    handleProofOfMisbehaviourMessage(sender, proofOfMisbehaviorMessage);
            }

            case ConfirmRequestMessage confirmRequestMessage -> {
                if (this.disgruntled) {
                    return;
                }
                handleConfirmRequestMessage(sender, confirmRequestMessage);
            }

            case ViewConfirmMessage viewConfirmMessage -> handleViewConfirmMessage(sender, viewConfirmMessage);

            case NewViewMessage newViewMessage -> handleNewViewMessage(sender, newViewMessage);

            case CheckpointMessage checkpointMessage -> handleCheckpointMessage(sender, checkpointMessage);

            case SpeculativeResponse speculativeResponse -> handleSpeculativeResponse(sender, speculativeResponse);

            case FillHoleMap fillHoleMap -> handleFillHoleMap(sender, fillHoleMap);

            default -> log.warning("Unknown message type: " + m.getType() + " from " + sender);
        }
    }

    private void handleFillHoleMap(String sender, FillHoleMap fillHoleMap) {
        for (Map.Entry<Long, FillHoleReply> entry : fillHoleMap.getFillHoleMap().entrySet()) {
            handleFillHoleReply(sender, entry.getValue());
        }
    }

    /**
     * Calculate the history hash for a given digest
     *
     * @param digest - the digest of the message
     * @return - the history hash
     */
    public long calculateHistory(long sequenceNumber, byte[] digest) {
        if (sequenceNumber > this.getHighestSequenceNumber() + 1) {
            log.warning("Replica " + this.getId() + " tried to calculate history hash for a sequence number that is too high expected: " + (this.getHighestSequenceNumber() + 1) + " received: " + sequenceNumber);
            return -1;
        }
        try {
            this.getHistory().get(sequenceNumber - 1);
        } catch (IndexOutOfBoundsException e) {
            log.warning("Trying to calculate history hash for a sequence number that is too low expected: " + (this.getHighestSequenceNumber() + 1) + " received: " + sequenceNumber);
            throw e;
        }
        return calculateHistoryFromPrevAndDigest(this.getHistory().get(sequenceNumber - 1), Arrays.hashCode(digest));
    }

    public long calculateHistoryFromPrevAndDigest(long prevHistory, int digestHashCode) {
        return prevHistory ^ digestHashCode;
    }

    /**
     * Update the history hash in the speculative history
     *
     * @param digest - the digest of the message to add to the history
     */
    public void updateHistory(long sequenceNumber, byte[] digest) {
        this.getHistory().add(sequenceNumber, calculateHistory(sequenceNumber, digest));
    }

    /**
     * Set the view number and the primary ID
     *
     * @param viewNumber - the view number to set
     */
    public void setView(long viewNumber) {
        log.info("Replica " + this.getId() + " setting view number to " + viewNumber);
        this.setView(viewNumber, this.computePrimaryId(viewNumber));
    }

    /**
     * Computes the primary ID for a given view number
     *
     * @param viewNumber - the view number
     * @return - the primary ID
     */
    private String computePrimaryId(long viewNumber) {
        // is this fine? it's a sorted set after all so we retain order
        return (String) this.getNodeIds().toArray()[(int) (viewNumber % this.getNodeIds().size())];
    }

    /**
     * Handle a request received from a client.
     * Corresponds to A2 in the paper
     *
     * @param clientId the ID of the client
     * @param request  the request payload
     */
    @Override
    public void handleClientRequest(String clientId, Serializable request) {
        RequestMessage requestMessage = (RequestMessage) request;
        // if this replica is the primary, then it can order the request
        if (this.getId().equals(this.getLeaderId()) && requestMessage.getTimestamp() > this.getMessageLog().highestTimestampInCacheForClient(clientId)) {
            byte[] digest = this.digest(request);
            OrderedRequestMessage orm = new OrderedRequestMessage(
                    // view number
                    this.getViewNumber(),
                    // assign a sequence number to the request
                    this.getHighestSequenceNumber() + 1,
                    // history
                    // hn = H(hn−1, d) is a digest summarizing the history
                    this.calculateHistory(this.getHighestSequenceNumber() + 1, digest),
                    // digest
                    digest);
            log.info("Replica " + this.getId() + " ordering request with sequence number " + orm.getSequenceNumber() + " and operation " + requestMessage.getOperation());
            orm.sign(this.getId());
            OrderedRequestMessageWrapper ormw = new OrderedRequestMessageWrapper(orm, requestMessage);
            this.broadcastMessage(ormw);
//            this.broadcastMessageIncludingSelf(ormw);
            this.handleOrderedRequestMessageWrapper(this.getId(), ormw);
        } else if (this.getId().equals(this.getLeaderId())) {
            log.warning("Retrieving cache as primary");
            SpeculativeResponseWrapper srw = this.getMessageLog().getResponseCache().get(clientId).getRight();
            this.sendMessage(srw, clientId);
        } else {
            this.getMessageLog().setLastRequest(clientId, requestMessage);
            forwardToPrimary(clientId, requestMessage);
        }
    }

    public void handleConfirmRequestMessage(String sender, ConfirmRequestMessage crm) {
        if (!this.getId().equals(this.getLeaderId())) {
            log.warning("Received confirm request message as non-primary");
            return;
        }
        if (crm.getViewNumber() != this.getViewNumber()) {
            log.warning("Received a confirm request message with an incorrect view number");
            return;
        }
        if (!crm.isSignedBy(sender)) {
            log.warning("Received a confirm request message with an invalid signature");
            return;
        }
        // check if the request is in the cache
        if (crm.getRequestMessage().getTimestamp() <= this.getMessageLog().highestTimestampInCacheForClient(crm.getRequestMessage().getClientId())) {
            OrderedRequestMessage orm = this.getMessageLog().getResponseCache().get(crm.getRequestMessage().getClientId()).getRight().getOrderedRequest();
            OrderedRequestMessageWrapper ormw = new OrderedRequestMessageWrapper(orm, crm.getRequestMessage());
            this.sendMessage(ormw, sender);
        } else {
            this.handleClientRequest(crm.getRequestMessage().getClientId(), crm.getRequestMessage());
        }
    }

    /**
     * Handle an ordered request message wrapper received by this replica and sends response to the client
     * Corresponds to A3 in the paper
     *
     * @param sender - The sender of the message
     * @param ormw   - The ordered request message wrapper that was received
     */
    public void handleOrderedRequestMessageWrapper(String sender, OrderedRequestMessageWrapper ormw) {
        if (!this.getLeaderId().equals(ormw.getOrderedRequest().getSignedBy())) {
            log.warning("Received an ordered request message from a non-primary replica, expected: " + this.getLeaderId() + " received: " + ormw.getOrderedRequest().getSignedBy());
            return;
        }
        try {
            if (this.getMessageLog().getRequestCache().containsKey(ormw.getRequestMessage().getClientId()) && this.getMessageLog().getRequestCache().get(ormw.getRequestMessage().getClientId()).equals(ormw.getRequestMessage())) {
                log.info("Cleared forward to primary timeout");
                this.clearTimeout(this.getForwardToPrimaryTimeoutId());
            }
        } catch (IllegalArgumentException e) {
            log.warning("Failed to clear request timeout, possibly because it's been triggered");
        }

        // reject the message if it's too far ahead
        long lastCheckpoint = this.getMessageLog().getLastCheckpoint();
        if (lastCheckpoint + (2L * this.CP_INTERVAL) + 1 < ormw.getOrderedRequest().getSequenceNumber()) {
            log.warning("Received an ordered request message after 2x CP_INTERVAL since last committed checkpoint, resending checkpoint message");
            CheckpointMessage cm = new CheckpointMessage(
                    lastCheckpoint,
                    this.getHistory().get(lastCheckpoint),
                    this.getId()
            );
            cm.sign(this.getId());
            this.broadcastMessage(cm);
            return;
        }
        try {
            this.clearTimeout(this.getRequestTimeoutId());
        } catch (IllegalArgumentException e) {
            log.warning("Failed to clear request timeout, possibly because it's been triggered");
        }
        if (this.isFillHoleActive()) {
            this.getMessageLog().putOrderedRequestMessageWrapper(ormw);
            return;
        }

        // check if the ordered request message is valid
        // IMPORTANT: as the primary, we don't check if it's valid because it won't allow for byzantine behavior
        // !this.getId().equals(this.getLeaderId()) &&
        if (!this.isValidOrderedRequestMessage(ormw.getOrderedRequest(), ormw.getRequestMessage())) {
            return;
        }

        try {
            if (this.getMessageLog().getRequestCache().get(ormw.getRequestMessage().getClientId()).equals(ormw.getRequestMessage())) {
                log.info("Cleared forward to primary timeout");
                this.clearTimeout(this.getForwardToPrimaryTimeoutId());
            }
        } catch (IllegalArgumentException e) {
            log.warning("Failed to clear forward to primary timeout, possibly because it's been triggered");
        } catch (NullPointerException ignored) {
        }
        SpeculativeResponseWrapper srw = this.executeOrderedRequest(ormw);

        // checkpointing
        if (ormw.getOrderedRequest().getSequenceNumber() % this.getCP_INTERVAL() == 0) {
            log.info("Replica " + this.getId() + " going to checkpoint");
            this.broadcastMessage(srw.getSpecResponse());
            this.handleSpeculativeResponse(this.getId(), srw.getSpecResponse());
        }
//
//        this.setRequestTimeoutId(this.setTimeout(
//                "request",
//                () -> {
//                    log.warning("Request timeout triggered");
//                    IHateThePrimaryMessage ihtpm = new IHateThePrimaryMessage(this.getViewNumber());
//                    ihtpm.sign(this.getId());
//                    this.broadcastMessage(ihtpm);
//                    this.handleIHateThePrimaryMessage(this.getId(), ihtpm);
//                },
//                Duration.ofMillis(30000)
//        ));
    }

    /**
     * Executes a given ordered request message and does the following:
     * - Creates a speculative response
     * - Saves to the speculative responses in the message log
     * - Saves to the ordered requests in the message log
     * - Updates the request cache
     * - Checkpoints if necessary
     * @param ormw - the ordered request message wrapper
     * @return - the speculative response wrapper
     */
    public SpeculativeResponseWrapper executeOrderedRequest(OrderedRequestMessageWrapper ormw) {
        // get the client ID from the request message
        String clientId = ormw.getRequestMessage().getClientId();
        SpeculativeResponseWrapper srw = this.handleOrderedRequestMessage(ormw.getOrderedRequest(), ormw.getRequestMessage());
        // saves to message log
        // updates the ordered messages
        this.getMessageLog().getOrderedMessages().put(ormw.getOrderedRequest().getSequenceNumber(), ormw);
        // updates the request cache
        this.getMessageLog().putResponseCache(clientId, ormw.getRequestMessage(), srw);

        // noop message
        if (ormw.getRequestMessage().getClientId().equals("Noop")) {
            log.info("Received a noop");
            return srw;
        }
        this.sendReplyToClient(ormw.getRequestMessage().getClientId(), srw);

        return srw;
    }

    /**
     * Handles an ordered request message by creating a speculative response. Does the following:
     * - Sets the highest sequence number
     * - Updates the speculative history
     * - Creates a speculative response and returns
     * Corresponds to A3 in the paper
     *
     * @param orm - the ordered request message
     * @param m   - the request message
     * @return - the speculative response wrapper
     */
    public SpeculativeResponseWrapper handleOrderedRequestMessage(OrderedRequestMessage orm, RequestMessage m) {
        // sets values and updates the history
        if (this.getHighestSequenceNumber() > orm.getSequenceNumber()) {
            log.warning("Received an ordered request message with a smaller than expected sequence number");
        }
        this.setHighestSequenceNumber(orm.getSequenceNumber());
        // this happens after a view change, no need to worry
        if (this.getHistory().getSize() == 0) {
            this.getHistory().add(orm.getSequenceNumber(), orm.getHistory());
        } else {
            this.updateHistory(orm.getSequenceNumber(), orm.getDigest());
        }

        SpeculativeResponse sr = new SpeculativeResponse(
                this.getViewNumber(),
                orm.getSequenceNumber(),
                orm.getHistory(),
                orm.getDigest(),
                m.getClientId(),
                m.getTimestamp()
        );

        sr.sign(this.getId());
        return new SpeculativeResponseWrapper(sr, this.getId(), m, orm);
    }

    /**
     * Checks if the ordered request message is valid
     * Corresponds to the first half of A3 in the paper
     *
     * @param orm - the ordered request message
     * @param m   - the request message
     * @return - true if the ordered request message is valid, false otherwise
     */
    public boolean isValidOrderedRequestMessage(OrderedRequestMessage orm, RequestMessage m) {
        // check if the view number is correct
        if (orm.getViewNumber() != this.getViewNumber()) {
            log.warning("Received an ordered request message with an incorrect view number");
            return false;
        }

        // check if the primary is correct
        if (!orm.isSignedBy(this.getLeaderId())) {
            log.warning("Received an ordered request message with an incorrect primary");
            return false;
        }

        // check (n == max_n + 1)
        if (orm.getSequenceNumber() != this.getHighestSequenceNumber() + 1) {
            // send fillHoles
            if (orm.getSequenceNumber() > this.getHighestSequenceNumber() + 1) {
                log.warning("Received an ordered request message with an larger than expected sequence number");
                this.fillHole(this.getHighestSequenceNumber() + 1, orm.getSequenceNumber());
            } else if (orm.getSequenceNumber() < this.getHighestSequenceNumber()) {
                log.warning("Received an ordered request message with a smaller than expected sequence number, init view change");
                /// TODO: Delete this, it's not in the protocol, but for now it's useful for testing the view changes
                IHateThePrimaryMessage ihtpm = new IHateThePrimaryMessage(this.getViewNumber());
                ihtpm.sign(this.getId());
                this.broadcastMessage(ihtpm);
                this.handleIHateThePrimaryMessage(this.getId(), ihtpm);
            } else {
                log.warning("Received the previous order request, could be due to a confirm request");
            }
            return false;
        }

        byte[] messageDigest = this.digest(m);

        // check if the digest is correct
        if (!Arrays.equals(orm.getDigest(), messageDigest)) {
            log.warning("Received an ordered request message with an incorrect digest");
            return false;
        }

        // check if the history hash is correct
        if (orm.getHistory() != this.calculateHistory(orm.getSequenceNumber(), messageDigest)) {
            log.warning("Replica " + this.getId() + " received an ordered request message with an incorrect history hash");
            return false;
        }
        return true;
    }

    /**
     * Initiates the Fill Hole procedure by sending a fill hole message to the primary
     *
     * @param receivedSequenceNumber - the sequence number of the message that was received
     */
    public void fillHole(long expectedSequenceNumber, long receivedSequenceNumber) {
        FillHoleMessage fhm = new FillHoleMessage(
                this.getViewNumber(),
                expectedSequenceNumber,
                receivedSequenceNumber,
                this.getId()
        );
        fhm.sign(this.getId());
        this.sendMessage(fhm, this.getLeaderId());

        // set the fill hole variables
        this.setFillHoleMin(this.getHighestSequenceNumber() + 1);
        this.setFillHoleMax(receivedSequenceNumber);
        this.setFillHoleActive(true);
        this.setFillHoleTimeoutId(this.setTimeout(
                "fillHolePrimary",
                () -> {
                    // check that we have all the responses, if we do, and they're valid, return
                    if (this.receivedFillHole(this.getFillHoleMin(), this.getFillHoleMax())) {
                        this.lapsedFillHole();
                        return;
                    }
                    this.broadcastMessage(fhm);
                    this.setFillHoleTimeoutAllId(this.setTimeout(
                            "fillHoleAll",
                            () -> {
                                if (this.receivedFillHole(this.getFillHoleMin(), this.getFillHoleMax())) {
                                    this.lapsedFillHole();
                                } else {
                                    log.warning("Failed to fill hole, init view change");
                                    IHateThePrimaryMessage ihtpm = new IHateThePrimaryMessage(this.getViewNumber());
                                    ihtpm.sign(this.getId());
                                    this.broadcastMessage(ihtpm);
                                    this.handleIHateThePrimaryMessage(this.getId(), ihtpm);
                                }
                            },
                            Duration.ofMillis(5000)
                    ));
                },
                Duration.ofMillis(5000)
        ));
    }

    /**
     * Executes the ordered request messages between the low and high sequence numbers
     *
     * @param lowSeqNum  - the first sequence number to fill (inclusive)
     * @param highSeqNum - the last sequence number to fill (inclusive
     */
    private void executeFillHoleReplies(long lowSeqNum, long highSeqNum) {
        // add to message logs
        for (long i = lowSeqNum; i <= highSeqNum; i++) {
            // we've already check that it's valid
            if (this.getMessageLog().getFillHoleMessages().get(i) == null) {
                log.warning("Received a null fill hole message during fill-hole");
            }
            FillHoleReply fhr = this.getMessageLog().getFillHoleMessages().get(i).sequencedValues().getFirst();

            OrderedRequestMessageWrapper ormw = new OrderedRequestMessageWrapper(
                    fhr.getOrderedRequestMessage(),
                    fhr.getRequestMessage()
            );
            if (!this.isValidOrderedRequestMessage(ormw.getOrderedRequest(), ormw.getRequestMessage())) {
                log.warning("Received an invalid ordered request message during fill-hole");
                return;
            }
            // handles the ordered request message
            this.executeOrderedRequest(ormw);
        }
    }

    /**
     * Handles the fill hole message request by validating and calling sendFillHoleReplies().
     *
     * @param sender          - The sender of the fill hole message
     * @param fillHoleMessage - The fill hole message that was received
     */
    private void handleFillHoleMessageRequest(String sender, FillHoleMessage fillHoleMessage) {
        if (fillHoleMessage.getViewNumber() < this.getViewNumber()) {
            log.warning("Received a fill hole message with a lower view number from " + fillHoleMessage.getReplicaId());
        } else if (fillHoleMessage.getViewNumber() > this.getViewNumber()) {
            log.warning("Received a fill hole message with a higher view number");
        }
        else if (fillHoleMessage.getReceivedSequenceNumber() > this.getHighestSequenceNumber()) {
            log.warning("Received a fill hole message with a larger than expected sequence number");
        } else {
            sendFillHoleReplies(fillHoleMessage.getReplicaId(), fillHoleMessage.getExpectedSequenceNumber(), fillHoleMessage.getReceivedSequenceNumber());
        }
    }

    /**
     * Sends the fill hole replies to the replica that requested them.
     *
     * @param sender                  - The sender of the fill hole message
     * @param lastKnownSequenceNumber - The last sequence number of the replica (inclusive)
     * @param receivedSequenceNumber  - The received sequence number of the replica (inclusive)
     */
    private void sendFillHoleReplies(String sender, long lastKnownSequenceNumber, long receivedSequenceNumber) {
        try {
            // try sending if it exists
            SortedMap<Long, OrderedRequestMessageWrapper> ormwMap = this.getMessageLog().getOrderedMessages().subMap(lastKnownSequenceNumber, receivedSequenceNumber + 1);
            FillHoleMap fhm = getFillHoleMap(ormwMap);
            this.sendMessage(fhm, sender);
        } catch (NullPointerException e) {
            log.warning("Replica " + this.getId() + " does not have the message with sequence number " + " while trying to reply to a fill-hole");
        }
    }

    /**
     * Creates a fill hole map from the ordered request message wrappers
     * @param ormwMap - the ordered request message wrappers
     * @return - the fill hole map
     */
    private FillHoleMap getFillHoleMap(SortedMap<Long, OrderedRequestMessageWrapper> ormwMap) {
        SortedMap<Long, FillHoleReply> fhrMap = new TreeMap<>();
        for (Map.Entry<Long, OrderedRequestMessageWrapper> entry : ormwMap.entrySet()) {
            FillHoleReply fhr = new FillHoleReply(
                    entry.getValue().getOrderedRequest(),
                    entry.getValue().getRequestMessage()
            );
            fhr.sign(this.getId());
            fhrMap.put(entry.getKey(), fhr);
        }
        FillHoleMap fhm = new FillHoleMap(fhrMap);
        fhm.sign(this.getId());
        return fhm;
    }

    /**
     * Handles the fill hole message reply by adding the message to the message log.
     *
     * @param sender        - The sender of the fill hole reply
     * @param fillHoleReply - The fill hole reply that was received
     */
    private void handleFillHoleReply(String sender, FillHoleReply fillHoleReply) {
        // if from the wrong view number, ignore
        if (fillHoleReply.getOrderedRequestMessage().getViewNumber() != this.getViewNumber()) {
            log.warning("Received a fill hole reply with a different view number");
        } else {
            // add to the fillHoleReply map in the message log
            this.getMessageLog().putFillHoleMessage(fillHoleReply);
            // check if all filled
            if (this.receivedFillHole(this.getFillHoleMin(), this.getFillHoleMax())) {
                // if all filled, execute the ordered request messages
                this.lapsedFillHole();
            }
        }
    }

    /**
     * When we've received our fill hole messages, we do the following:
     * - Clear the fill hole timeouts
     * - Execute the requests
     * - Set the fill hole variables to inactive
     * - Check from the max to the maximum ordered request message if there are any holes and fill them
     */
    private void lapsedFillHole() {
        try {
            this.clearTimeout(this.getFillHoleTimeoutId());
        } catch (IllegalArgumentException e) {
            log.warning("Failed to clear fill hole timeout, possibly because it's been triggered");
        }
        try {
            this.clearTimeout(this.getFillHoleTimeoutAllId());
        } catch (IllegalArgumentException e) {
            log.warning("Failed to clear fill hole timeout all, possibly because it's been triggered");
        }
        // executes the received fill hole messages
        this.executeFillHoleReplies(this.getFillHoleMin(), this.getFillHoleMax());
        SequencedSet<Long> orderedRequestKeySet = this.getMessageLog().getOrderedMessages().sequencedKeySet();
        SortedSet<Long> missingOrderedRequests = new TreeSet<>();
        // check if we have any missing ordered requests after the fill_hole_max + 1
        for (long i = this.getFillHoleMax() + 1; i <= orderedRequestKeySet.getLast(); i++) {
            if (!this.getMessageLog().getOrderedMessages().containsKey(i)) {
                missingOrderedRequests.add(i);
            }
        }
        long lastFilled = this.getFillHoleMax();
        this.setFillHoleActive(false);
        this.setFillHoleMin(-1);
        this.setFillHoleMax(-1);
        // if we have missing ordered requests, fill them
        if (!missingOrderedRequests.isEmpty()) {
            this.fillHole(missingOrderedRequests.first(), missingOrderedRequests.last());
        }
        // otherwise, execute them ordered request messages
        else {
            for (long i = lastFilled + 1; i <= orderedRequestKeySet.getLast(); i++) {
                this.handleOrderedRequestMessageWrapper("noop", this.getMessageLog().getOrderedMessages().get(i));
            }
        }
    }

    /**
     * Checks if all fill holes have been received between the expected and received sequence numbers (inclusive)
     * @param expectedSequenceNumber - the expected sequence number
     * @param receivedSequenceNumber - the received sequence number
     * @return - true if all fill holes have been received, false otherwise
     */
    private boolean receivedFillHole(long expectedSequenceNumber, long receivedSequenceNumber) {
        // check that all fill holes have been received
        for (long i = expectedSequenceNumber; i <= receivedSequenceNumber; i++) {
            // if we miss messages, return false
            if (!this.getMessageLog().getFillHoleMessages().containsKey(i)) {
                return false;
            } else {
                Set<FillHoleReply> fillHoleReplies = new HashSet<>(this.getMessageLog().getFillHoleMessages().get(i).values());
                if (fillHoleReplies.size() > 1) {
                    List<OrderedRequestMessage> orderedRequestMessages = fillHoleReplies.stream().map(FillHoleReply::getOrderedRequestMessage).toList();
                    // create a proof of misbehaviour
                    OrderedRequestMessage orm1 = orderedRequestMessages.getFirst();
                    OrderedRequestMessage orm2 = orderedRequestMessages.getLast();
                    ProofOfMisbehaviorMessage pom = new ProofOfMisbehaviorMessage(
                            this.getViewNumber(),
                            new ImmutablePair<>(orm1, orm2)
                    );
                    pom.sign(this.getId());
                    this.handleProofOfMisbehaviourMessage(this.getId(), pom);
                    this.broadcastMessage(pom);
                    return false;
                }
            }
        }
        log.info("Replica " + this.getId() + " received all fill hole messages between " + expectedSequenceNumber + " and " + receivedSequenceNumber);
        return true;
    }

    /**
     * Handles the Commit message by updating the commit log and maxCC
     * @param sender - the sender of the message
     * @param commitMessage - the commit message that was received
     */
    private void handleCommitMessage(String sender, CommitMessage commitMessage) {
        // check if the commit message is valid
        CommitCertificate cc = commitMessage.getCommitCertificate();

        if (cc.getSequenceNumber() <= this.getMessageLog().getMaxCC().getSequenceNumber()) {
            LocalCommitMessage lcm = new LocalCommitMessage(
                    cc.getViewNumber(),
                    cc.getDigest(),
                    cc.getHistory(),
                    this.getId(),
                    sender
            );
            lcm.sign(this.getId());
            this.sendMessage(lcm, sender);
            return;
        }

        // check validity
        if (!this.isValidCommitCertificate(cc)) {
            log.warning("Received invalid commit certificate from " + sender);
            return;
        }
        log.info("Received a commit certificate from " + sender + " with sequence number " + cc.getSequenceNumber());
        // commit the operations
        this.handleCommitCertificate(cc);
        LocalCommitMessage lcm = new LocalCommitMessage(
                cc.getViewNumber(),
                this.getMessageLog().getOrderedMessages().get(cc.getSequenceNumber()).getOrderedRequest().getDigest(),
                cc.getHistory(),
                this.getId(),
                sender);
        log.info("Replica " + this.getId() + " sending a local commit message to " + sender);
        lcm.sign(this.getId());
        this.sendMessage(lcm, sender);
    }


    /**
     * Updates to the most recent commit certificate and commits the operations
     *
     * @param cc - the client-sent commit certificate
     */
    private void handleCommitCertificate(CommitCertificate cc) {
        CommitCertificate currentCC = this.getMessageLog().getMaxCC();

        // get the last committed sequence number
        long oldSeqNum = (currentCC != null) ? currentCC.getSequenceNumber() : 0;
        if (this.getMessageLog().getLastCheckpoint() > oldSeqNum) {
            oldSeqNum = this.getMessageLog().getLastCheckpoint();
        }

        // commit the operation to the log
        for (long i = oldSeqNum + 1; i <= cc.getSequenceNumber(); i++) {
            try {
                this.commitOperation(i, new SerializableLogEntry(this.getMessageLog().getOrderedMessages().get(i).getRequestMessage().getOperation()));
            } catch (NullPointerException e) {
                log.warning("Replica " + this.getId() + " does not have the message with sequence number " + i);
                log.warning("Last checkpoint is: " + this.getMessageLog().getLastCheckpoint());
                log.warning("Last CC is: " + this.getMessageLog().getMaxCC().getSequenceNumber());
                log.warning("Sequence numbers in the log: " + this.getMessageLog().getOrderedMessages().keySet());
                throw e;
            } catch (IllegalArgumentException exception) {
                throw exception;
            }
        }
        log.info("Replica " + this.getId() + " committed up to sequence number " + cc.getSequenceNumber());
        this.getMessageLog().setMaxCC(cc);
        if (this.getCommitLog().getHighestSequenceNumber() > this.getMessageLog().getMaxCC().getSequenceNumber()) {
            log.warning("Replica " + this.getId() + " has a higher sequence number in the commit log than the maxCC");
            throw new IllegalStateException("Replica " + this.getId() + " has a higher sequence number in the commit log than the maxCC");
        }
    }

    /**
     * Checks if the commit certificate has the right number of replicas and the speculative responses match
     *
     * @param cc - the commit certificate to check
     * @return - true if the commit certificate is valid, false otherwise
     */
    private boolean isValidCommitCertificate(CommitCertificate cc) {
//        if (cc.getViewNumber() != this.getViewNumber()) {
//            log.warning("Received a commit certificate with an incorrect view number");
//            return false;
//        }
        // 2f + 1 property
        if (new HashSet<>(cc.getSignedBy()).size() < 2 * this.faultsTolerated + 1) {
            log.warning("Received a commit certificate with an insufficient number of replicas");
            return false;
        }

        // check to fill holes
        if (cc.getSequenceNumber() > this.getHighestSequenceNumber()) {
            log.warning("Received a commit certificate with a larger than expected sequence number. Expected: " + this.getHighestSequenceNumber() + " Received: " + cc.getSequenceNumber());
            log.info("Filling holes");
            this.fillHole(this.getHighestSequenceNumber() + 1, cc.getSequenceNumber());
            return false;
        }


        // check if the history matches
        if (cc.getHistory() != this.getHistory().get(cc.getSequenceNumber())) {
            log.warning("Received a commit certificate with an incorrect history hash, init view change");
            IHateThePrimaryMessage ihtpm = new IHateThePrimaryMessage(this.getViewNumber());
            ihtpm.sign(this.getId());
            this.broadcastMessage(ihtpm);
            this.handleIHateThePrimaryMessage(this.getId(), ihtpm);
            return false;
        }

        CommitCertificate currentCC = this.getMessageLog().getMaxCC();

        if (currentCC != null && cc.getSpeculativeResponse().getSequenceNumber() < currentCC.getSequenceNumber()) {
            log.warning("Received a commit certificate with a lower sequence number, received " + cc.getSequenceNumber() + " expected " + currentCC.getSequenceNumber() + 1);
            return false;
        }

        // if the currentCC is not null and the sequence number is the same, we return true, this means we commit again?
        if (currentCC != null && cc.getSequenceNumber() == currentCC.getSequenceNumber()) {
            log.warning("Replica " + this.getId() + " received a commit certificate with the same sequence number ");
            return false;
        }

        return true;
    }

    /**
     * Forwards the receipt of a request to the primary replica
     * Corresponds to A4c in the paper
     *
     * @param clientId - the client ID
     * @param rm       - the request message
     */
    public void forwardToPrimary(String clientId, RequestMessage rm) {
        CommitCertificate maxCC = this.getMessageLog().getMaxCC();
        // if the request timestamp is lower or equal to the one that we have, we send the last ordered request
        if (rm.getTimestamp() <= this.getMessageLog().highestTimestampInCacheForClient(clientId)) {
            SpeculativeResponseWrapper srw = this.getMessageLog().getResponseCache().get(clientId).getRight();
            this.sendMessage(srw, clientId);
            // return a local commit as well
            log.info("Replica " + this.getId() + " sending a speculative response to " + clientId + " with digest " + Arrays.hashCode(this.getMessageLog().getMaxCC().getDigest()));
            if (maxCC != null && maxCC.getSpeculativeResponse().getTimestamp() >= rm.getTimestamp()) {
                LocalCommitMessage lcm = new LocalCommitMessage(
                        // view number
                        this.getViewNumber(),
                        // client digest
                        this.getMessageLog().getMaxCC().getDigest(),
                        // history
                        maxCC.getSequenceNumber(),
                        // replicaId
                        this.getId(),
                        // clientId
                        clientId
                );
                log.info("Replica " + this.getId() + " sent a local commit message to " + clientId + " with digest " + Arrays.toString(lcm.getDigest()));
                lcm.sign(this.getId());
                this.sendMessage(lcm, clientId);
            }
        } else {
            // we send to the primary
            ConfirmRequestMessage crm = new ConfirmRequestMessage(
                    this.getViewNumber(),
                    rm,
                    this.getId()
            );
            crm.sign(this.getId());
            this.sendMessage(crm, this.getLeaderId());
            log.info("Replica " + this.getId() + " sent a confirm request message to " + this.getLeaderId());

            this.getMessageLog().putRequestCache(clientId, rm);

            /// TODO: would be nice to include the ADP here but not necessary
            this.setForwardToPrimaryTimeoutId(this.setTimeout(
                    "forwardToPrimary",
                    () -> {
                        // we don't check if we've received here because in most cases, we get more recent requests
                        // and we don't know if we've received it. So we check the property in the handleOrderedRequestMessage()
                        log.warning("Failed to forward to primary, init view change");
                        IHateThePrimaryMessage ihtpm = new IHateThePrimaryMessage(this.getViewNumber());
                        ihtpm.sign(this.getId());
                        this.broadcastMessage(ihtpm);
                        this.handleIHateThePrimaryMessage(this.getId(), ihtpm);
                    },
                    // give it time in case we need to fill holes
                    Duration.ofMillis(15000)
            ));
        }
    }

    /**
     * Handles a POM message
     * It sends out an I hate the primary message and forwards the POM message
     * @param sender - the sender of the message (ignored in this case)
     * @param pom - the proof of misbehaviour message
     */
    private void handleProofOfMisbehaviourMessage(String sender, ProofOfMisbehaviorMessage pom) {
        // we don't check the sender since we have to forward the message.
        if (pom.getViewNumber() != this.getViewNumber()) {
            log.warning("Received a proof of misbehaviour message with a different view number");
        }
        // if we've already sent a pom, we should've started a new view.
        if (this.getMessageLog().getLastPOM() == this.getViewNumber()) {
            return;
        }
        OrderedRequestMessage orm1 = pom.getPom().getLeft();
        OrderedRequestMessage orm2 = pom.getPom().getRight();
        // check for the right sequence number and that the messages are not equal
        if (orm1.getSequenceNumber() == orm2.getSequenceNumber() && !orm1.equals(orm2)) {
            // send an I hate the primary message and a proof of misbehaviour
            log.warning("Received a proof of misbehaviour message, init view change");
            IHateThePrimaryMessage ihtpm = new IHateThePrimaryMessage(this.getViewNumber());
            ihtpm.sign(this.getId());
            this.handleIHateThePrimaryMessage(this.getId(), ihtpm);
            this.broadcastMessage(ihtpm);
            this.broadcastMessage(pom);
            this.getMessageLog().setLastPOM(this.getViewNumber());
        } else {
            log.warning("Received a proof of misbehaviour message with equal ordered request messages");
        }

    }

    /**
     * Handles the I hate the primary message received by this replica by adding it to the message log
     * Also initiates a view change if f + 1 IHateThePrimary messages are received
     * Corresponds to VC1 in the paper
     *
     * @param sender - The sender of the message
     * @param ihtpm  - The I hate the primary message that was received
     */
    public void handleIHateThePrimaryMessage(String sender, IHateThePrimaryMessage ihtpm) {
        if (ihtpm.getViewNumber() < this.getViewNumber()) {
            log.warning("Received an I hate the primary message with a lower view number");
        } else if (!ihtpm.getSignedBy().equals(sender)) {
            log.warning("Received an I hate the primary message with an invalid signature");
        } else {
            this.getMessageLog().putIHateThePrimaryMessage(ihtpm);
            // if f + 1 ihatetheprimaries
            int numIHateThePrimaries = this.numberIHateThePrimaries(List.copyOf(this.getMessageLog().getIHateThePrimaries().getOrDefault(this.getViewNumber(), new TreeMap<>()).values()));
            if (numIHateThePrimaries >= this.faultsTolerated + 1) {
                // we move on to vc2
                this.commitToViewChange();
            }
        }
    }

    /**
     * Commits to a view change by sending a view change message
     * Uses either the maxCC, the view confirm messages, or the last view change message as the CC
     * Corresponds to VC2 in the paper
     */
    private void commitToViewChange() {
        List<IHateThePrimaryMessage> iHateThePrimaryMessages = List.copyOf(this.getMessageLog().getIHateThePrimaries().get(this.getViewNumber()).values());
        // redundant check but okay
        if (iHateThePrimaryMessages.size() < this.faultsTolerated + 1) {
            log.warning("Number of IHateThePrimary messages is less than f + 1");
            return;
        }

        CommitCertificate cc = this.getMessageLog().getMaxCC();
        if (cc == null) {
            log.warning("MaxCC is null");
        }

        if (this.getMessageLog().getCheckpointMessages().get(this.getMessageLog().getLastCheckpoint()) == null) {
            log.warning("Checkpoint messages is null");
        }
        ViewChangeMessage vcm = new ViewChangeMessage(
                // future view number
                this.getViewNumber() + 1,
                // last checkpoint
                this.getMessageLog().getLastCheckpoint(),
                // checkpoint messages
                this.getMessageLog().getCheckpointMessages().get(this.getMessageLog().getLastCheckpoint()).values().stream().toList(),
                // commit certificate
                cc,
                // ordered requests since last checkpoint
                this.getMessageLog().getOrderedRequestHistory(this.getMessageLog().getLastCheckpoint()),
                this.getId()
        );
        vcm.sign(this.getId());
        ViewChangeMessageWrapper vcmw = new ViewChangeMessageWrapper(iHateThePrimaryMessages, vcm);

        if (!this.disgruntled) {
            this.broadcastMessage(vcmw);
            this.handleViewChangeMessageWrapper(this.getId(), vcmw);
        }
        log.info("Replica " + this.getId() + " committed to a view change with maxCC " + cc.getSequenceNumber() + ", last checkpoint " + this.getMessageLog().getLastCheckpoint() + " and highest sequence number " + this.getHighestSequenceNumber());
        this.setDisgruntled(true);
    }

    /**
     * Handles the view change message wrapper received by this replica
     * Corresponds to the reception of VC3 in the paper
     *
     * @param sender                   - The sender of the message
     * @param viewChangeMessageWrapper - The view change message wrapper that was received
     */
    private void handleViewChangeMessageWrapper(String sender, ViewChangeMessageWrapper viewChangeMessageWrapper) {
        ViewChangeMessage vcm = viewChangeMessageWrapper.getViewChangeMessage();
        long futureViewNumber = vcm.getFutureViewNumber();

        // discard previous views
        if (futureViewNumber <= this.getViewNumber()) {
            log.warning("Replica " +
                    this.getId() +
                    " received a view change message with an incorrect view number from " +
                    sender +
                    ". Expected " +
                    (this.getViewNumber() + 1) +
                    ", got " +
                    futureViewNumber +
                    ". This is usually because we've already changed the view and then received this so it's fine."
                    );
            return;
        }
        // check if valid
        if (!this.isValidViewChangeMessageWrapper(viewChangeMessageWrapper)) {
            log.warning("Received an invalid view change message wrapper");
            return;
        }

        // puts the view change message in the view change messages list
        this.getMessageLog().putViewChangeMessage(vcm);

        // if we have more than 2f + 1 view change messages, we go on to VC3
        // they have already been validated by the time they reach this point
        // note: this can be for any view number > current view number, so v + 100 for example is also valid here
        if (this.getMessageLog()
                .getViewChangeMessages()
                .get(vcm.getFutureViewNumber())
                .size() >=
                (2 * this.faultsTolerated + 1)) {
            // go on to VC3
            this.viewChange(vcm.getFutureViewNumber());
        }
    }

    /**
     * Checks if a given view change message wrapper is valid with the following properties:
     * - There are at least f + 1 distinct IHateThePrimary messages
     * - The view change message is valid
     * @param vcmw - the view change message wrapper to check
     * @return - if the vcmw is valid
     */
    private boolean isValidViewChangeMessageWrapper(ViewChangeMessageWrapper vcmw) {
        // check that there are enough valid IHateThePrimary messages from the view change message
        if (!(numberIHateThePrimaries(vcmw.getIHateThePrimaries()) >= this.faultsTolerated + 1)) {
            log.warning("Received a view change message with an incorrect number of IHateThePrimary messages");
            return false;
        }

        return isValidViewChangeMessage(vcmw.getViewChangeMessage());
    }

    /**
     * Checks if a given view change message is valid with the following properties:
     * - Checkpoint messages are signed by the correct replica
     * - Checkpoint messages are for the stable checkpoint
     * - There are 2f + 1 unique replicas
     * @param vcm - the view change message to check
     * @return - if the vcm is valid
     */
    private boolean isValidViewChangeMessage(ViewChangeMessage vcm) {
        List<CheckpointMessage> checkpoints = vcm.getCheckpoints();
        long stableCheckpoint = vcm.getStableCheckpoint();
        Set<String> replicaIds = new HashSet<>();

        for (CheckpointMessage cm : checkpoints) {
            if (!cm.isSignedBy(cm.getReplicaId())) {
                // signed property
                log.warning("Received a view change message with an invalid signature");
            } else if (cm.getSequenceNumber() != stableCheckpoint) {
                // sequence number property
                log.warning("Received a view change message with an incorrect checkpoint");
            } else {
                replicaIds.add(cm.getReplicaId());
            }
        }
        return replicaIds.size() >= 2 * this.faultsTolerated + 1;
    }

    /**
     * Counts the number of unique IHateThePrimary messages from a given list
     * @param ihtps - the list of IHateThePrimary messages
     * @return - the number of unique IHateThePrimary messages
     */
    private int numberIHateThePrimaries(List<IHateThePrimaryMessage> ihtps) {
        Set<String> ihtpSet = new HashSet<>();
        for (IHateThePrimaryMessage ihtp : ihtps) {
            ihtpSet.add(ihtp.getSignedBy());
        }
        return ihtpSet.size();
    }

    /**
     * Corresponds to VC3 in the paper
     */
    private void viewChange(long newViewNumber) {
        if (this.computePrimaryId(newViewNumber).equals(this.getId())) {
            this.viewChangePrimary(newViewNumber);
        } else {
            this.viewChangeReplica(newViewNumber);
        }
    }

    /**
     * VC3 for the primary
     * Creates the new-view replica and sends it to all replicas
     * @param newViewNumber - the view number for the one we're about to enter
     */
    private void viewChangePrimary(long newViewNumber) {
        // somewhat redundant check since we already did this but eh
        if (this.getMessageLog().getViewChangeMessages().get(newViewNumber).size() < 2 * this.faultsTolerated + 1) {
            log.warning("Received a view change message with an incorrect number of replicas");
            return;
        }
        // redundant and should be impossible
        if (newViewNumber <= this.getViewNumber()) log.warning("Initiating a new view with a smaller number");

        // create new-view message
        NewViewMessage nvm = new NewViewMessage(
                newViewNumber,
                this.getMessageLog().getViewChangeMessages().get(newViewNumber),
                computeOrderedRequestHistory(newViewNumber, this.getMessageLog().getViewChangeMessages().get(newViewNumber))
        );
        nvm.sign(this.getId());
        this.broadcastMessage(nvm);
        this.handleNewViewMessage(this.getId(), nvm);
    }

    /// TODO: check everything's signed and the view numbers are correct
    /**
     * Calculates the ordered request history as a primary for a view change
     * This method is way too big I hate it
     * Corresponds to VC3 in the paper
     * @param newViewNumber - the view number for the one we're about to enter
     * @param viewChangeMessagesMap - the view change messages map (replicaId -> view change message)
     * @return - the ordered request history as a sorted set
     */
    public SortedMap<Long, OrderedRequestMessageWrapper> computeOrderedRequestHistory(long newViewNumber, SortedMap<String, ViewChangeMessage> viewChangeMessagesMap) {
        // min-s
        long latestStableCheckpoint = 0;
        // max-cc
        long highestCommittedSequenceNumber = 0;
        // max-r
        long highestNonCommittedSequenceNumber = 0;
        // max-s
        long highestSequenceNumber = 0;

        // replica used to get between min-s and max-cc
        String maxCCReplica = "";
        // return value
        SortedMap<Long, OrderedRequestMessageWrapper> orderedRequestHistory = new TreeMap<>();
        // used for max-cc to max-s
        SortedMap<Long, List<OrderedRequestMessageWrapper>> orderedRequests = new TreeMap<>();

        // the viewChangeMessages have already been validated by the time they reach this point
        // sets the values for latestStableCheckpoint and highestCommittedSequenceNumber
        for (ViewChangeMessage vcm : viewChangeMessagesMap.values()) {
            // updates the highest stable checkpoint (min-s)
            if (vcm.getStableCheckpoint() >= latestStableCheckpoint) {
                latestStableCheckpoint = vcm.getStableCheckpoint();
            }
            // updates the highest committed sequence number (max-cc)
            if (vcm.getCommitCertificate().getSequenceNumber() >= highestCommittedSequenceNumber) {
                highestCommittedSequenceNumber = vcm.getCommitCertificate().getSequenceNumber();
                maxCCReplica = vcm.getReplicaId();
            }
            // updates the highest sequence number (max-s)
            if (!vcm.getOrderedRequestHistory().isEmpty() && vcm.getOrderedRequestHistory().lastKey() > highestSequenceNumber) {
                highestSequenceNumber = vcm.getOrderedRequestHistory().lastKey();
            }
            // used to get the collective ordered request history of all the replicas
            // add all the ordered requests to the map for further processing
            for (OrderedRequestMessageWrapper ormw : vcm.getOrderedRequestHistory().values()) {
                if (ormw == null) {
                    log.warning("Null ordered request message wrapper while computing ordered request history");
                    continue;
                }
                orderedRequests.putIfAbsent(ormw.getOrderedRequest().getSequenceNumber(), new ArrayList<>());
                orderedRequests.get(ormw.getOrderedRequest().getSequenceNumber()).add(ormw);
            }
        }

        if (orderedRequests.isEmpty()) {
            log.info("Empty ordered requests while computing ordered request history");
            return orderedRequestHistory;
        }

        // Put upto max-cc (inclusive)
        // Committed requests
        SortedMap<Long, OrderedRequestMessageWrapper> committedRequests = viewChangeMessagesMap.get(maxCCReplica).getOrderedRequestHistory().headMap(highestCommittedSequenceNumber + 1);
        for (long i = latestStableCheckpoint + 1; i <= highestCommittedSequenceNumber; i++) {
            OrderedRequestMessageWrapper ormw = committedRequests.get(i);
            // update the view number
            OrderedRequestMessage newOrm = ormw.getOrderedRequest().withViewNumber(newViewNumber);
            OrderedRequestMessageWrapper newOrmw = new OrderedRequestMessageWrapper(newOrm, ormw.getRequestMessage());
            newOrmw.sign(this.getId());
            // add to the ordered request history
            orderedRequestHistory.put(i, newOrmw);
        }

        // used to keep track of the last request in order to calculate history
        long prevHistory = 0;
        if (committedRequests.containsKey(highestCommittedSequenceNumber)) {
            prevHistory = committedRequests.get(highestCommittedSequenceNumber).getOrderedRequest().getHistory();
        }
        // Put upto max-r (inclusive)
        long currSeqNum = highestCommittedSequenceNumber + 1;
        while (currSeqNum <= orderedRequests.sequencedKeySet().getLast()) {
            HashMap<OrderedRequestMessageWrapper, Integer> frequencies = new HashMap<>();
            // gets the list of ordered request messages for the current sequence number that we're checking
            List<OrderedRequestMessageWrapper> ormwList = orderedRequests.get(currSeqNum);
            // add to frequencies
            for (OrderedRequestMessageWrapper ormw : ormwList) {
                frequencies.put(ormw, frequencies.getOrDefault(ormw, 0) + 1);
            }

            // filter out by if there's more than f + 1 ormws and sort by frequency
            Stack<Map.Entry<OrderedRequestMessageWrapper, Integer>> filtered = frequencies.entrySet()
                    .stream()
                    // filter by if there's more than f + 1
                    .filter(entry -> entry.getValue() >= this.getFaultsTolerated() + 1)
                    // sort by the frequency in a descending order
                    .sorted(Map.Entry.<OrderedRequestMessageWrapper, Integer>comparingByValue().reversed())
                    // go to stack
                    .collect(Collectors.toCollection(Stack::new));

            // if the stack is empty, then we move onto the requests that are guaranteed not to have completed
            if (filtered.isEmpty()) {
                break;
            }
            // tries to find a correct history
            OrderedRequestMessageWrapper correct = filtered.pop().getKey();
            long correctHistory = correct.getOrderedRequest().getHistory();
            int digestHash = (correct.getRequestMessage() != null) ? Arrays.hashCode(this.digest(correct.getRequestMessage())) : 0;
            long calculatedHistory = calculateHistoryFromPrevAndDigest(prevHistory, digestHash);
            // goes through the stack and finds a valid history
            while(!filtered.isEmpty() && correctHistory != calculatedHistory) {
                correct = filtered.pop().getKey();
                correctHistory = correct.getOrderedRequest().getSequenceNumber();
                digestHash = (correct.getRequestMessage() != null) ? Arrays.hashCode(this.digest(correct.getRequestMessage())) : 0;
                calculatedHistory = calculateHistoryFromPrevAndDigest(prevHistory, digestHash);
            }
            // if we do find a match, we add it to the history
            if (correctHistory == calculatedHistory) {
                // update the view number
                OrderedRequestMessage newOrm = correct.getOrderedRequest().withViewNumber(newViewNumber);
                OrderedRequestMessageWrapper newOrmw = new OrderedRequestMessageWrapper(newOrm, correct.getRequestMessage());
                newOrmw.sign(this.getId());
                // add to the ordered request history
                orderedRequestHistory.put(currSeqNum, newOrmw);
                prevHistory = newOrmw.getOrderedRequest().getHistory();
            } else {
                break;
            }
            currSeqNum++;
        }

        highestNonCommittedSequenceNumber = currSeqNum - 1;

        // create the null requests now to be executed as noops
        for (long i = highestNonCommittedSequenceNumber + 1; i <= highestSequenceNumber; i++) {
            if (i == 0) {
                log.warning("Noop created with sequence number 0");
                continue;
            }
            RequestMessage rm = new RequestMessage(
                    "Noop/" + i,
                    i,
                    "Noop"
            );
            byte[] digest = this.digest(rm);
            OrderedRequestMessage orm = new OrderedRequestMessage(
                    newViewNumber,
                    i,
                    calculateHistoryFromPrevAndDigest(prevHistory, Arrays.hashCode(digest)),
                    digest
            );
            orm.sign(this.getId());
            OrderedRequestMessageWrapper ormw = new OrderedRequestMessageWrapper(orm, rm);
            orderedRequestHistory.put(i, ormw);
            prevHistory = orm.getHistory();
        }

        return orderedRequestHistory;
    }

    /**
     * VC3 for the replica
     * Sets a timeout for receiving a new view message
     * @param newViewNumber - the view number for the one we're about to enter
     */
    public void viewChangeReplica(long newViewNumber) {
        // we grow the timeout exponentially
        long lastViewNumber = 0;
        try {
            lastViewNumber = this.getMessageLog().
                    getNewViewMessages().
                    get(this.getMessageLog()
                            .getNewViewMessages()
                            .lastKey())
                    .getFutureViewNumber();
        } catch (NoSuchElementException e) {
            // if we don't have any new view messages, we set the last view number to 0
        }
        long timeout = (long) Math.pow(2, (newViewNumber - lastViewNumber)) * 5000;

        this.setTimeout(
                "viewChangeReplica",
                () -> {
                    // if we have received a new-view message, then we return
                    if (this.getMessageLog().getNewViewMessages().containsKey(newViewNumber)) {
                        log.info("Replica " + this.getId() + " received a new view message for view number " + newViewNumber);
                        return;
                    }
                    log.warning("Haven't received a new view message in time, init view change");
                    // if we haven't received a new-view message, then we send a ihatetheprimary message
                    IHateThePrimaryMessage ihtpm = new IHateThePrimaryMessage(newViewNumber + 1);
                    ihtpm.sign(this.getId());
                    this.broadcastMessage(ihtpm);
                    this.handleIHateThePrimaryMessage(this.getId(), ihtpm);
                    // It's still disgruntled so we don't actually accept messages etc.
                    // But we need to update the view number for everything else
                    // For example if we receive a new-view message even though the timeout has passed
                    log.info("Setting view number to " + newViewNumber);
                    this.setView(newViewNumber);

                },
                Duration.ofMillis(timeout)
        );
    }

    /**
     * Checks if a new view message is valid by checking the following properties:
     * - Signed by correct primary
     * - View change messages are signed by the correct replica
     * - Number of replicas is correct
     */
    public boolean isValidNewViewMessage(NewViewMessage nvm) {
        // check if the primary is correct
        if (!nvm.isSignedBy(this.computePrimaryId(nvm.getFutureViewNumber()))) {
            log.warning("Received a new view message with an incorrect primary");
        }

        Set<String> replicaIds = new HashSet<>();
        for (ViewChangeMessage vcm : nvm.getViewChangeMessages().values()) {
            if(this.isValidViewChangeMessage(vcm)) replicaIds.add(vcm.getReplicaId());
        }

        // check if the number of replicas is correct
        if (replicaIds.size() < 2 * this.faultsTolerated + 1) {
            log.warning("Received a new view message with an incorrect number of replicas");
            return false;
        }
        return true;
    }

    /**
     * Adds the viewConfirmMessage to the message log and changes the view if the number of view confirm messages is correct
     * Corresponds to VC5 in the paper
     *
     * @param sender - The sender of the message
     * @param vcm    - The view confirm message that was received
     */
    public void handleViewConfirmMessage(String sender, ViewConfirmMessage vcm) {
        // check if the vcm is signed by the sender
        if (!vcm.isSignedBy(sender)) {
            log.warning("Received a view confirm message with an invalid signature");
            return;
        }
        // ignore smaller view numbers
        if (vcm.getFutureViewNumber() <= this.getViewNumber()) {
            log.warning("Current view number: " + this.getViewNumber());
            log.warning("Replica " +
                    this.getId() +
                    " received a view confirm message with a smaller than expected view number from " +
                    sender +
                    ". Expected " +
                    (this.getViewNumber() + 1) +
                    " got " +
                    vcm.getFutureViewNumber() +
                    ". Got " +
                    this.getMessageLog().getViewConfirmMessages().getOrDefault(vcm.getFutureViewNumber(), new TreeSet<>()).size() +
                    " for that view."
        );
            return;
        }

        // add the view confirm message to the view confirm messages list
        this.getMessageLog().putViewConfirmMessage(vcm);
        if (this.getMessageLog().getNewViewMessages().get(vcm.getFutureViewNumber()) != null) {
            checkViewConfirmMessages(vcm.getFutureViewNumber());
        }
    }

    /**
     * Reconciles the local history with the view change messages
     * @param viewChangeMessages - the view change messages to reconcile with (provided in the new view message)
     * @param calculatedHistory - the calculated history from the view change messages (provided in the new view message) (corresponds to G in the extended technical report)
     */
    private void reconcileLocalHistoryViewChange(Collection<ViewChangeMessage> viewChangeMessages, SortedMap<Long, OrderedRequestMessageWrapper> calculatedHistory) {
        long latestStableCheckpoint = viewChangeMessages.stream().map(ViewChangeMessage::getStableCheckpoint).max(Long::compareTo).orElse(-1L);
        CommitCertificate maxCC = viewChangeMessages.stream().map(ViewChangeMessage::getCommitCertificate).max(Comparator.comparingLong(CommitCertificate::getSequenceNumber)).get();
        log.info("Replica " + this.getId() + " reconciling local history with maxCC " + maxCC.getSequenceNumber() + ", latest stable checkpoint " + latestStableCheckpoint + " and highest sequence number " + this.getHighestSequenceNumber());

        if (this.getMessageLog().getLastCheckpoint() > latestStableCheckpoint) {
            log.warning("Received a view change message with a lower than expected latest stable checkpoint");
            this.getMessageLog().getOrderedMessages().clear();
            this.getMessageLog().getSpeculativeResponsesCheckpoint().clear();
            this.getHistory().rollback(this.getMessageLog().getLastCheckpoint());
            return;
        }

        if (calculatedHistory.isEmpty()) {
            // nothing to reconcile
            if (latestStableCheckpoint == this.getHighestSequenceNumber()) return;
            else if (latestStableCheckpoint < this.getHighestSequenceNumber()) {
                rollbackToCheckpoint(latestStableCheckpoint, calculatedHistory, maxCC);
                return;
            }
            else {
                catchUpToCheckpoint(latestStableCheckpoint, calculatedHistory, maxCC);
                return;
            }
        }

        // in case the primary didn't receive our view-change message
        // we roll back to the last checkpoint and execute up to G
        // we return because we're up to speed then
        if (calculatedHistory.sequencedKeySet().getLast() < this.getHighestSequenceNumber()) {
            this.rollbackToCheckpoint(latestStableCheckpoint, calculatedHistory, maxCC);
            return;
        }

        // max-l < min-s
        if (this.getHighestSequenceNumber() < latestStableCheckpoint) {
            this.catchUpToCheckpoint(latestStableCheckpoint, calculatedHistory, maxCC);
        }
        // max-l == min-s
        else if (this.getHighestSequenceNumber() == latestStableCheckpoint) {
            // execute from max-l + 1
            for (OrderedRequestMessageWrapper ormw : calculatedHistory.sequencedValues()) {
                this.executeOrderedRequest(ormw);
            }
            // if we have a higher commit certificate, we set it
            this.handleCommitCertificate(maxCC);
        }
        // max-l > min-s
        else {
            long lastHistoryKey;
            try {
                lastHistoryKey = this.getHistory().getLastKey();
            } catch (NoSuchElementException e) {
                log.warning("No last history key found");
                return;
            }

            long lastHistory = this.getHistory().get(lastHistoryKey);
            if (calculatedHistory.get(lastHistoryKey).getOrderedRequest().getHistory() == lastHistory) {
                // handle the last commit certificate (make sure everything is committed)
                // execute from max-l + 1

                for (long i = this.getHighestSequenceNumber() + 1; i <= calculatedHistory.sequencedKeySet().getLast(); i++) {
                    this.executeOrderedRequest(calculatedHistory.get(i));
                }
                if (this.getMessageLog().getLastCheckpoint() < latestStableCheckpoint) {
                    this.getMessageLog().setLastCheckpoint(latestStableCheckpoint);
                }

                if (this.getMessageLog().getMaxCC().getSequenceNumber() < maxCC.getSequenceNumber()) {
                    this.handleCommitCertificate(maxCC);
                }
            }
            // histories diverge and we roll back
            else {
                if (this.getMessageLog().getMaxCC().getSequenceNumber() > maxCC.getSequenceNumber()) {
                    log.info("Diverging histories, rolling back to checkpoint");
                    this.getMessageLog().getOrderedMessages().clear();
                    this.getHistory().clear();
                    this.getMessageLog().getRequestCache().clear();
                    for (OrderedRequestMessageWrapper ormw : calculatedHistory.sequencedValues()) {
                        if (ormw.getOrderedRequest().getSequenceNumber() <= this.getMessageLog().getMaxCC().getSequenceNumber()) {
                            continue;
                        }
                        this.executeOrderedRequest(ormw);
                    }

                } else {
                    this.rollbackToCheckpoint(latestStableCheckpoint, calculatedHistory, maxCC);
                }
            }
        }

    }

    /**
     * Catches up to the checkpoint by doing the following:
     * - Sets the latest checkpoint to the received one
     * - Clears the history and the ordered request messages in the log
     * - Sets the highest sequence number to the latest checkpoint
     * - Executes any future requests
     * - Handles the maxCC
     * @param latestStableCheckpoint - the latest stable checkpoint received
     * @param calculatedHistory - the ormw history calculated from the view change messages (G in the extended technical report)
     * @param maxCC - the maxCC received from the view change messages
     */
    private void catchUpToCheckpoint(long latestStableCheckpoint, SortedMap<Long, OrderedRequestMessageWrapper> calculatedHistory, CommitCertificate maxCC) {
        log.info("Replica " + this.getId() + " catching up to checkpoint " + latestStableCheckpoint);
        // set the latest checkpoint
        this.getMessageLog().setLastCheckpoint(latestStableCheckpoint);
        // clear the history
        this.getHistory().clear();
        // clear the message log
        this.getMessageLog().getOrderedMessages().clear();
        this.setHighestSequenceNumber(latestStableCheckpoint);
        // executes the requests from the last checkpoint
        for (OrderedRequestMessageWrapper ormw : calculatedHistory.sequencedValues()) {
            this.executeOrderedRequest(ormw);
        }
        // commits any more of the requests if they exist
        this.handleCommitCertificate(maxCC);
    }

    /**
     * Rolls back to the previous stable checkpoint by doing the following:
     * - Removes the checkpoint responses, ordered messages and request cache
     * - Clears the history
     * - Sets the highest sequence number to the latest stable checkpoint
     * - Executes the requests in the calculated history
     * - Handles the maxCC
     * @param latestStableCheckpoint - the latest stable checkpoint
     * @param maxCC - the maxCC received from the view change messages
     */
    private void rollbackToCheckpoint(long latestStableCheckpoint, SortedMap<Long, OrderedRequestMessageWrapper> calculatedHistory, CommitCertificate maxCC) {
        log.info("Replica " + this.getId() + " rolling back to checkpoint " + latestStableCheckpoint);
        // remove everything in the message logs
        this.getMessageLog().getSpeculativeResponsesCheckpoint().clear();
        this.getMessageLog().getOrderedMessages().clear();
        this.getHistory().clear();
        this.setHighestSequenceNumber(latestStableCheckpoint);
        this.getMessageLog().getRequestCache().clear();

        // executes the requests in the calculatedHistory
        for (OrderedRequestMessageWrapper ormw : calculatedHistory.sequencedValues()) {
            this.executeOrderedRequest(ormw);
        }
        if (maxCC.getSequenceNumber() > this.getHighestSequenceNumber()) {
            log.warning("MaxCC sequence number is greater than the highest sequence number");
        }
        // handles the commit certificate
        this.handleCommitCertificate(maxCC);
    }

    /**
     * Checks the view confirm messages criteria for the future view number
     * @param futureViewNumber - the future view number to check for
     */
    private void checkViewConfirmMessages(long futureViewNumber){
        // calculate the frequencies of the view confirm messages
        HashMap<ViewConfirmMessage, Integer> frequencies = new HashMap<>();
        // count the number of view confirm messages for each view number
        for (ViewConfirmMessage viewConfirmMessage : this.getMessageLog().getViewConfirmMessages().get(futureViewNumber)) {
            frequencies.put(viewConfirmMessage, frequencies.getOrDefault(viewConfirmMessage, 0) + 1);
        }

        // get the max frequency
        Map.Entry<ViewConfirmMessage, Integer> maxEntry = frequencies.entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .orElse(null);

        if (maxEntry == null) {
            log.warning("No view confirm messages found for view number " + futureViewNumber);
            return;
        }

        // if we have enough view confirm messages, we change the view
        if (maxEntry.getValue() >= 2 * this.faultsTolerated + 1) {
            this.beginNewView(maxEntry.getKey());

        } else{
            log.info("Not enough view confirm messages for view number " + futureViewNumber);
        }
    }

    /**
     * Handles the new view message received by this replica
     * Corresponds to VC4 in the paper
     * @param sender - the sender of the message
     * @param nvm - the received new view message
     */
    private void handleNewViewMessage(String sender, NewViewMessage nvm) {
        long maxCCSeqNum = nvm.getViewChangeMessages().values().stream().map(ViewChangeMessage::getCommitCertificate).map(CommitCertificate::getSequenceNumber).max(Long::compareTo).orElse(-1L);
        long latestStableCheckpoint = nvm.getViewChangeMessages().values().stream().map(ViewChangeMessage::getStableCheckpoint).max(Long::compareTo).orElse(-1L);
        log.info("Replica " + this.getId() + " received a new view message from " + sender + " for view number " + nvm.getFutureViewNumber() + " with maxCC " + maxCCSeqNum + " and latest stable checkpoint " + latestStableCheckpoint);
        log.info("Current maxCC: " + this.getMessageLog().getMaxCC().getSequenceNumber() + " and last checkpoint: " + this.getMessageLog().getLastCheckpoint());
        // check if for the right view number
        if (nvm.getFutureViewNumber() <= this.getViewNumber()) {
            log.warning("Replica " +
                    this.getId() +
                    " received a new view message with a smaller than expected view number from " +
                    sender +
                    " expected " +
                    (this.getViewNumber() + 1) +
                    " got " +
                    nvm.getFutureViewNumber());
            return;
        }

        // if it isn't valid, we reject
        if(!isValidNewViewMessage(nvm)) {
            return;
        }
        if (this.getMessageLog().getNewViewMessages().containsKey(nvm.getFutureViewNumber())) {
            log.info("Received a new view message for a view number that we've already received");
            return;
        }

        // add the new view message to the message log
        this.getMessageLog().putNewViewMessage(nvm);

        SortedMap<Long, OrderedRequestMessageWrapper> calculatedHistory = this.computeOrderedRequestHistory(nvm.getFutureViewNumber(), nvm.getViewChangeMessages());
        if (!equalHistories(calculatedHistory, nvm.getOrderedRequestHistory())) {
            log.warning("Received a new view message with an incorrect ordered request history");
            return;
        }

        // reconcile the local history with the view change messages
        this.reconcileLocalHistoryViewChange(nvm.getViewChangeMessages().values(), calculatedHistory);
        long history = 0;
        if (!(this.getHistory().getSize() == 0)) {
            history = this.getHistory().getLast();
        }

        ViewConfirmMessage vcm = new ViewConfirmMessage(
                nvm.getFutureViewNumber(),
                this.getHighestSequenceNumber(),
                history,
                this.getId()
                );
        log.info("Replica " + this.getId() + " sending view confirm message for view number " + nvm.getFutureViewNumber());
        vcm.sign(this.getId());
        this.broadcastMessage(vcm);
        // puts it into the view confirm messages
        this.handleViewConfirmMessage(this.getId(), vcm);
        // checks if the criteria for the view confirm messages are met
        // if so, then we begin the new view
        this.checkViewConfirmMessages(nvm.getFutureViewNumber());
    }

    /**
     * Checks that the histories that we calculate in VC4 are equal
     * @param thisCalculatedHistory - the history that this replica calculated
     * @param primaryCalculatedHistory  - the history that the primary calculated
     * @return - if the histories are equal
     */
    public boolean equalHistories(SortedMap<Long, OrderedRequestMessageWrapper> thisCalculatedHistory, SortedMap<Long, OrderedRequestMessageWrapper> primaryCalculatedHistory) {
        // check if equal keysets
        if (!thisCalculatedHistory.keySet().equals(primaryCalculatedHistory.keySet())) return false;
        for (long key : thisCalculatedHistory.keySet()) {
            if (!thisCalculatedHistory.get(key).equals(primaryCalculatedHistory.get(key))) return false;
        }
        return true;
    }

    /**
     * Begins the new view
     * Corresponds to VC5 in the paper
     *
     * @param vcm - The view confirm message that was received
     */
    private void beginNewView(ViewConfirmMessage vcm) {
        // clear view specific messages
        this.getMessageLog().getIHateThePrimaries().getOrDefault(this.getViewNumber(), new TreeMap<>()).clear();
        this.getMessageLog().getFillHoleMessages().clear();

        // sets the view number and primary
        log.info("Replica " +
                this.getId() +
                " beginning new view " +
                vcm.getFutureViewNumber() +
                " with history: " +
                vcm.getLastKnownSequenceNumber() +
                " and sequence number: " +
                this.getHighestSequenceNumber()
                );
        if (this.getHistory().getSize() == 0) {
            this.getHistory().add(vcm.getLastKnownSequenceNumber(), vcm.getHistory());
        }
        this.setView(vcm.getFutureViewNumber());
        this.clearAllTimeouts();
        this.setDisgruntled(false);
    }

    private void handleCheckpointMessage(String sender, CheckpointMessage cm) {
        // check if the i == signed
        if (!cm.isSignedBy(cm.getReplicaId())) {
            log.warning("Received a checkpoint message with an invalid signature");
            return;
        }

        // if the sequence number is lower than the last checkpoint, we ignore it
        if (cm.getSequenceNumber() == this.getMessageLog().getLastCheckpoint()) {
            return;
        }

        if (cm.getSequenceNumber() < this.getMessageLog().getLastCheckpoint()) {
            log.warning("Received a checkpoint message with a lower than expected sequence number");
            return;
        }

        // add the checkpoint message to the message log
        this.getMessageLog().putCheckpointMessage(cm);

        // if the sequence number is the maxCC, we check if we can commit to the checkpoint
        if (this.getMessageLog().getMaxCC().getSequenceNumber() == cm.getSequenceNumber()) {
            this.checkIfCommitCheckpoint(cm.getSequenceNumber());
        } else log.warning("Waiting for request " + cm.getSequenceNumber() + " to be received before committing to the checkpoint");
    }

    /**
     * Checks if the criteria for a checkpoint is reached, if we consider the checkpoint stable and call the createCheckpoint()
     * The method exists because we could receive f + 1 checkpoint messages before we create our commit certificate, so we add a check before
     */
    private void checkIfCommitCheckpoint(long sequenceNumber) {
        if (this.getMessageLog().getMaxCC().getSequenceNumber() < sequenceNumber) {
            log.warning("Replica " + this.getId() + " didn't create a commit certificate for sequence number " + sequenceNumber);
        }
        SortedMap<String, CheckpointMessage> currCheckpoints = this.getMessageLog().getCheckpointMessages().get(sequenceNumber);

        HashMap<CheckpointMessage, Integer> frequencies = new HashMap<>();
        for (CheckpointMessage checkpointMessage : currCheckpoints.values()) {
            frequencies.put(checkpointMessage, frequencies.getOrDefault(checkpointMessage, 0) + 1);
        }
        // replica receives at least f + 1 matching checkpoint messages for the checkpoint and considers it stable
        // find the max frequency
        Map.Entry<CheckpointMessage, Integer> maxEntry = frequencies.entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .orElse(null);

        if (maxEntry == null) {
            log.warning("No checkpoint messages found for sequence number " + sequenceNumber);
            return;
        } else if (maxEntry.getValue() < 2 * this.getFaultsTolerated() + 1) {
            return;
        } else if (maxEntry.getKey().getSequenceNumber() > this.getHighestSequenceNumber()) {
            log.warning("Received a checkpoint message with a larger than expected sequence number");
            this.fillHole(this.getHighestSequenceNumber() + 1, maxEntry.getKey().getSequenceNumber());
            return;
        }
        // if everything's fine, we create a checkpoint
        this.createCheckpoint(maxEntry.getKey().getSequenceNumber());
    }

    /**
     * Creates a checkpoint and truncates our history
     * @param sequenceNumber - the sequence number to create the checkpoint for
     */
    private void createCheckpoint(long sequenceNumber) {
        log.info("Replica " + this.getId() + " created stable checkpoint for sequence number " + sequenceNumber);
        // set the last checkpoint
        this.getMessageLog().setLastCheckpoint(sequenceNumber);

        // garbage collect the previous history
        long checkpointSequenceNumber = this.getMessageLog().getLastCheckpoint();
//        this.getHistory().truncate(sequenceNumber);
        this.getMessageLog().truncateCheckpointMessages(checkpointSequenceNumber);
        this.getMessageLog().truncateOrderedRequestMessages(checkpointSequenceNumber);
        this.getMessageLog().truncateFillHoleMessages(checkpointSequenceNumber);
    }

    /**
     * Handles the speculative response message received by this replica
     * @param sender - the sender of the message
     * @param sr - the speculative response message that was received
     */
    private void handleSpeculativeResponse(String sender, SpeculativeResponse sr) {
        // if not signed by sender
        if (!sr.isSignedBy(sender)) {
            log.warning("Received a speculative response with an invalid signature");
            return;
        }

        // if the sequence number is lower than the last checkpoint, we ignore it
        if (sr.getSequenceNumber() <= this.getMessageLog().getLastCheckpoint()) {
            log.warning("Replica " + this.getId() + " received a speculative response with a lower than expected sequence number by replica " + sender);
            return;
        }

        // put in the message log
        this.getMessageLog().putSpeculativeResponseCheckpoint(sr.getSequenceNumber(), sr);
        if (this.getHighestSequenceNumber() >= sr.getSequenceNumber()) {
            this.checkIfCheckpoint(sr.getSequenceNumber());
        }
    }

    /**
     * Checks if the criteria for a checkpoint is reached, if so
     * we create a commit certificate, process it and send out a checkpoint message
     * @param sequenceNumber - the sequence number to check
     */
    private void checkIfCheckpoint(long sequenceNumber) {
        Map<String, SpeculativeResponse> specResponses = this.getMessageLog().getSpeculativeResponsesCheckpoint().get(sequenceNumber);

        // find the frequencies of the speculative responses received
        HashMap<SpeculativeResponse, Integer> frequencies = new HashMap<>();
        for (SpeculativeResponse speculativeResponse : specResponses.values()) {
            frequencies.put(speculativeResponse, frequencies.getOrDefault(speculativeResponse, 0) + 1);
        }

        // find the max frequency
        Map.Entry<SpeculativeResponse, Integer> maxEntry = frequencies.entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .orElse(null);

        // we have no speculative responses
        if (maxEntry == null) {
            log.warning("No speculative responses found for sequence number " + sequenceNumber);
            return;
        }

        // 2f + 1 property
        if (maxEntry.getValue() < 2 * this.getFaultsTolerated() + 1) {
            return;
        }

        List<String> signedBy = new ArrayList<>();

        // filter out the keys that have the value of the maxEntry in the specResponses
        for (Map.Entry<String, SpeculativeResponse> entry : specResponses.entrySet()) {
            if (entry.getValue().equals(maxEntry.getKey())) {
                signedBy.add(entry.getKey());
            }
        }
        SpeculativeResponse maxResponse = maxEntry.getKey();
        OrderedRequestMessageWrapper correspondingOrmw = this.getMessageLog().getOrderedMessages().get(sequenceNumber);
        if (maxResponse.getSequenceNumber() <= this.getMessageLog().getLastCheckpoint()) {
            return;
        }
        CommitCertificate currCC = new CommitCertificate(
                maxResponse.getSequenceNumber(),
                maxResponse.getViewNumber(),
                maxResponse.getHistory(),
                correspondingOrmw.getOrderedRequest().getDigest(),
                maxResponse,
                signedBy
        );
        // What happens when we receive f + 1 checkpoint messages before
        if (!this.isValidCommitCertificate(currCC)) {
            log.warning("Created an invalid commit certificate");
            return;
        }
        this.handleCommitCertificate(currCC);
        CheckpointMessage cm = new CheckpointMessage(
                maxResponse.getSequenceNumber(),
                maxResponse.getHistory(),
                this.getId()
        );
        cm.sign(this.getId());
        this.broadcastMessage(cm);
        log.info("Replica " + this.getId() + " sent a checkpoint message for sequence number " + sequenceNumber);
        this.handleCheckpointMessage(this.getId(), cm);
    }
}

// handleSpeculativeResponse -> preReq: this.seqNum > specResponse - checkIfCheckpoint -> handleCheckpointMessage
// handleCheckpointMessage -> checkIfCommitCheckpoint -> createCheckpoint