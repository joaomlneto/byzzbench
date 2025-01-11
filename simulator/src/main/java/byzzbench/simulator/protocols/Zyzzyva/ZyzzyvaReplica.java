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

/// TODO: check if everything is signed correctly
/// TODO: checkpoints
/// TODO: when checkpointing, create a new maxCC
/// TODO: Check all fillhole logic
@Log
@ToString(callSuper = true)
public class ZyzzyvaReplica extends LeaderBasedProtocolReplica {
    @Getter
    private final int CP_INTERVAL;

    @Getter
    private final int faultsTolerated;

    @Getter
    @Setter
    private long highestTimestamp = -1;

    @Getter
    @Setter
    private long highestSequenceNumber = -1;

    @Getter
    @Setter
    private boolean disgruntled = false;

    @Getter
    private final SortedSet<String> nodeIds;

    // used for the speculative history
    // we use the commit log for the committed history
    @Getter
    private final SpeculativeHistory history;

    @Getter
    @JsonIgnore
    private final MessageLog messageLog;

    public ZyzzyvaReplica(String replicaId,
                          SortedSet<String> nodeIds,
                          Scenario scenario,
                          int CP_INTERVAL) {
        super(replicaId, scenario, new TotalOrderCommitLog());
        this.nodeIds = nodeIds;

        this.history = new SpeculativeHistory();
        this.messageLog = new MessageLog(15);
        this.CP_INTERVAL = CP_INTERVAL;
        this.faultsTolerated = (nodeIds.size() - 1) / 3;

        CommitCertificate cc = new CommitCertificate(
                -1,
                0,
                -1L,
                new byte[0],
                new TreeMap<>(),
                "Self"
        );
        // used for view changes
        this.getMessageLog().setMaxCC(cc);
    }

    @Override
    public void initialize() {
        this.setView(0);
    }

    @Override
    public void handleMessage(String sender, MessagePayload m) throws Exception {
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

            case FillHoleReply fillHoleReply -> handleFillHoleMessageReply(sender, fillHoleReply);

            case CommitMessage commitmessage -> {
                if (this.disgruntled) {
                    return;
                }
                handleCommitMessage(sender, commitmessage);
            }

            case IHateThePrimaryMessage iHateThePrimaryMessage ->
                    handleIHateThePrimaryMessage(sender, iHateThePrimaryMessage);

            case ViewChangeMessageWrapper viewChangeMessageWrapper ->
                    handleViewChangeMessageWrapper(sender, viewChangeMessageWrapper);

            case ProofOfMisbehaviorMessage proofOfMisbehaviorMessage ->
                    handleProofOfMisbehaviourMessage(sender, proofOfMisbehaviorMessage);

            case ConfirmRequestMessage confirmRequestMessage -> {
                if (this.disgruntled) {
                    return;
                }
                handleConfirmRequestMessage(sender, confirmRequestMessage);
            }

            case ViewConfirmMessage viewConfirmMessage -> handleViewConfirmMessage(sender, viewConfirmMessage);

            case NewViewMessage newViewMessage -> handleNewViewMessage(sender, newViewMessage);

            default -> throw new RuntimeException("Unknown message type: " + m.getType());
        }
    }

    /**
     * Calculate the history hash for a given digest
     *
     * @param digest - the digest of the message
     * @return - the history hash
     */
    public long calculateHistory(byte[] digest) {
        return (this.getHistory().getLast() + Arrays.hashCode(digest));
    }

    /**
     * Update the history hash in the speculative history
     *
     * @param digest - the digest of the message to add to the history
     */
    public void updateHistory(long sequenceNumber, byte[] digest) {
        this.getHistory().add(sequenceNumber, calculateHistory(digest));
    }

    /**
     * Set the view number and the primary ID
     *
     * @param viewNumber - the view number to set
     */
    public void setView(long viewNumber) {
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
     * @throws Exception -
     */
    @Override
    public void handleClientRequest(String clientId, Serializable request) throws Exception {
        RequestMessage requestMessage = (RequestMessage) request;
        // if this replica is the primary, then it can order the request
        if (this.getId().equals(this.getLeaderId()) && requestMessage.getTimestamp() > this.highestTimestamp) {
            byte[] digest = this.digest(request);
            OrderedRequestMessage orm = new OrderedRequestMessage(
                    // view number
                    this.getViewNumber(),
                    // assign a sequence number to the request
                    this.getHighestSequenceNumber() + 1,
                    // history
                    // hn = H(hn−1, d) is a digest summarizing the history
                    this.calculateHistory(digest),
                    // digest
                    digest);
            orm.sign(this.getId());
            OrderedRequestMessageWrapper ormw = new OrderedRequestMessageWrapper(orm, requestMessage);
            this.broadcastMessageIncludingSelf(ormw);
        } else {
            forwardToPrimary(clientId, requestMessage);
        }
    }

    public void handleConfirmRequestMessage(String sender, ConfirmRequestMessage crm) throws Exception {
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
        if (crm.getRequestMessage().getTimestamp() <= this.highestTimestamp) {
            OrderedRequestMessageWrapper ormw = this.getMessageLog().getOrderedMessages().get(this.getHighestSequenceNumber());
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
        System.out.println("Received ordered request message");
        if (!this.getLeaderId().equals(sender)) {
            log.warning("Received an ordered request message from a non-primary replica");
            return;
        }
        // check if the ordered request message is valid
        if (this.isValidOrderedRequestMessage(ormw.getOrderedRequest(), ormw.getRequestMessage())) {
            // get the client ID from the request message
            String clientId = ormw.getRequestMessage().getClientId();
            SpeculativeResponseWrapper srw = this.handleOrderedRequestMessage(ormw.getOrderedRequest(), ormw.getRequestMessage());
            // send a speculative response to the client
            this.sendReplyToClient(clientId, srw);
            // saves to message log
            this.getMessageLog().getSpeculativeResponses().put(ormw.getOrderedRequest().getSequenceNumber(), srw.getSpecResponse());
            this.getMessageLog().getOrderedMessages().put(ormw.getOrderedRequest().getSequenceNumber(), ormw);
        } else {
            log.warning("Received an invalid ordered request message");
        }
    }

    /**
     * Handles an ordered request message by creating a speculative response
     * Corresponds to A3 in the paper
     *
     * @param orm - the ordered request message
     * @param m   - the request message
     * @return - the speculative response wrapper
     */
    public SpeculativeResponseWrapper handleOrderedRequestMessage(OrderedRequestMessage orm, RequestMessage m) {
        // sets values and updates the history
        this.setHighestSequenceNumber(orm.getSequenceNumber());
        this.setHighestTimestamp(m.getTimestamp());
        this.updateHistory(orm.getSequenceNumber(), orm.getDigest());

        SpeculativeResponse sr = new SpeculativeResponse(this.getViewNumber(),
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
                this.fillHole(orm.getSequenceNumber());
            } else {
                log.warning("Received an ordered request message with a smaller than expected sequence number");
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
        if (orm.getHistory() != this.calculateHistory(messageDigest)) {
            log.warning("Received an ordered request message with an incorrect history hash");
            return false;
        }
        return true;
    }

    /**
     * Initiates the Fill Hole procedure by sending a fill hole message to the primary
     *
     * @param receivedSequenceNumber - the sequence number of the message that was received
     */
    public void fillHole(long receivedSequenceNumber) {
        FillHoleMessage fhm = new FillHoleMessage(
                this.getViewNumber(),
                this.getHighestSequenceNumber() + 1,
                receivedSequenceNumber,
                this.getId()
        );
        fhm.sign(this.getId());
        this.sendMessage(fhm, this.getLeaderId());

        this.setTimeout(
                "fillHolePrimary",
                () -> {
                    // check that we have all the responses, if we do, and they're valid, return
                    if (this.receivedFillHole(receivedSequenceNumber)) {
                        long oldSeqNum = this.getHighestSequenceNumber();
                        this.fillOrderedRequestMessages(this.getLeaderId(), oldSeqNum + 1, receivedSequenceNumber);
                        return;
                    }
                    this.broadcastMessage(fhm);
                    this.setTimeout(
                            "fillHoleAll",
                            () -> {
                                if (this.receivedFillHole(receivedSequenceNumber)) {
                                    long oldSeqNum = this.getHighestSequenceNumber();
                                    this.fillOrderedRequestMessages(this.getLeaderId(), oldSeqNum + 1, receivedSequenceNumber);
                                } else {
                                    log.warning("Failed to fill hole");
                                }
                            },
                            Duration.ofMillis(5000)
                    );
                },
                Duration.ofMillis(5000));
    }

    /**
     * Fills the ordered request message log from the fill hole messages once found valid
     *
     * @param senderId   - the sender of the fill hole replies
     * @param lowSeqNum  - the first sequence number to fill (inclusive)
     * @param highSeqNum - the last sequence number to fill (inclusive
     */
    private void fillOrderedRequestMessages(String senderId, long lowSeqNum, long highSeqNum) {
        // add to message logs
        for (long i = lowSeqNum; i <= highSeqNum; i++) {
            FillHoleReply fhr = this.getMessageLog().getFillHoleMessages().get(i).get(senderId);
            OrderedRequestMessageWrapper ormw = new OrderedRequestMessageWrapper(
                    fhr.getOrderedRequestMessage(),
                    fhr.getRequestMessage()
            );
            if (!this.isValidOrderedRequestMessage(ormw.getOrderedRequest(), ormw.getRequestMessage())) {
                log.warning("Received an invalid ordered request message");
            }
            // handles the ordered request message
            SpeculativeResponseWrapper srw = this.handleOrderedRequestMessage(ormw.getOrderedRequest(), ormw.getRequestMessage());
            // add to the message log
            this.getMessageLog().getSpeculativeResponses().put(ormw.getOrderedRequest().getSequenceNumber(), srw.getSpecResponse());
            this.getMessageLog().getOrderedMessages().put(ormw.getOrderedRequest().getSequenceNumber(), ormw);
        }
    }

    /**
     * Handles the fill hole message request by validating and calling sendFillHoleReplies().
     *
     * @param sender          - The sender of the fill hole message
     * @param fillHoleMessage - The fill hole message that was received
     */
    private void handleFillHoleMessageRequest(String sender, FillHoleMessage fillHoleMessage) {
        if (this.getViewNumber() != fillHoleMessage.getViewNumber()) {
            log.warning("Received a fill hole message with a different view number");
        } else if (fillHoleMessage.getReceivedSequenceNumber() > this.getHighestSequenceNumber()) {
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
        for (long n = lastKnownSequenceNumber; n <= receivedSequenceNumber; n++) {
            try {
                // try sending if it exists
                OrderedRequestMessageWrapper ormw = this.getMessageLog().getOrderedMessages().get(n);
                this.sendMessage(ormw, sender);
            } catch (NullPointerException e) {
                log.warning("Replica " + this.getId() + " does not have the message with sequence number " + n);
            }
        }
    }

    /**
     * Handles the fill hole message reply by adding the message to the message log.
     *
     * @param sender        - The sender of the fill hole reply
     * @param fillHoleReply - The fill hole reply that was received
     */
    private void handleFillHoleMessageReply(String sender, FillHoleReply fillHoleReply) {
        /// TODO: Check if we need to add the spec response to the speculative response history
        if (fillHoleReply.getOrderedRequestMessage().getViewNumber() != this.getViewNumber()) {
            log.warning("Received a fill hole reply with a different view number");
        } else {
            // add to the fillHoleReply map in the message log
            this.getMessageLog().getFillHoleMessages().get(fillHoleReply.getOrderedRequestMessage().getSequenceNumber()).put(sender, fillHoleReply);
        }
    }

    private boolean receivedFillHole(long receivedSequenceNumber) {
        // check that all fill holes have been received
        for (long i = this.getHighestSequenceNumber() + 1; i <= receivedSequenceNumber; i++) {
            // if we miss messages, return false
            if (!this.getMessageLog().getFillHoleMessages().containsKey(i)) {
                return false;
            } else {
                SortedSet<FillHoleReply> fillHoleReplies = new TreeSet<>(this.getMessageLog().getFillHoleMessages().get(i).values());
                if (fillHoleReplies.size() > 1) {
                    // create a proof of misbehaviour
                    OrderedRequestMessage orm1 = fillHoleReplies.first().getOrderedRequestMessage();
                    OrderedRequestMessage orm2 = fillHoleReplies.last().getOrderedRequestMessage();
                    ProofOfMisbehaviorMessage pom = new ProofOfMisbehaviorMessage(
                            this.getViewNumber(),
                            new ImmutablePair<>(orm1, orm2)
                    );
                    pom.sign(this.getId());
                    this.broadcastMessageIncludingSelf(pom);
                    return false;
                }
            }
        }
        return true;
    }

    private void handleCommitMessage(String sender, CommitMessage commitMessage) {
        // check if the commit message is valid
        CommitCertificate cc = commitMessage.getCommitCertificate();
        if (!this.isValidCommitCertificate(cc)) {
            log.warning("Received an invalid commit message");
            return;
        }

        SpeculativeResponse firstResponse = cc.getSpeculativeResponses().firstEntry().getValue();

        try {
            // if the history is consistent, then we can commit the operations
            if (firstResponse.getHistory() == this.getHistory().get(firstResponse.getSequenceNumber())) {
                this.handleCommitCertificate(cc);
                LocalCommitMessage lcm = new LocalCommitMessage(
                        cc.getViewNumber(),
                        cc.getDigest(),
                        this.getHistory().get(cc.getSequenceNumber()),
                        this.getId(),
                        cc.getClientId());

                lcm.sign(this.getId());
                this.sendMessage(lcm, cc.getClientId());

                this.getHistory().truncate(firstResponse.getSequenceNumber());
            } else {
                // if the histories are diverging we initiate a view change
                // initiate a view change
                IHateThePrimaryMessage ihtpm = new IHateThePrimaryMessage(this.getViewNumber());
                ihtpm.sign(this.getId());
                this.broadcastMessageIncludingSelf(ihtpm);

            }
        } catch (IndexOutOfBoundsException e) {
            log.warning("Received a commit certificate with a history that doesn't exist");
            // This probably occurs because of a fillhole so we do exactly that
            if (firstResponse.getSequenceNumber() > this.getHighestSequenceNumber()) {
                this.fillHole(firstResponse.getSequenceNumber());
            } else {
                log.warning("Unknown error with the commit certificate");
            }
        }
    }


    /**
     * Updates to the most recent commit certificate and commits the operations
     *
     * @param cc - the client-sent commit certificate
     */
    private void handleCommitCertificate(CommitCertificate cc) {
        CommitCertificate currentCC = this.getMessageLog().getMaxCC();
        // if the client cc is more recent than our own, we commit the operations
        if (currentCC == null || cc.getSequenceNumber() > currentCC.getSequenceNumber()) {
            long oldSeqNum = (currentCC != null) ? currentCC.getSequenceNumber() + 1 : 0;
            for (long i = oldSeqNum; i <= cc.getSequenceNumber(); i++) {
                this.commitOperation(new SerializableLogEntry(this.getMessageLog().getOrderedMessages().get(i).getRequestMessage().getOperation()));
            }
            this.getMessageLog().setMaxCC(cc);
        }
    }

    /**
     * Checks if the commit certificate has the right number of replicas and the speculative responses match
     *
     * @param cc - the commit certificate to check
     * @return - true if the commit certificate is valid, false otherwise
     */
    private boolean isValidCommitCertificate(CommitCertificate cc) {
        if (cc.getSpeculativeResponses().isEmpty()) {
            log.warning("Received a commit certificate with no replicas");
            return false;
        }

        // check if the speculative responses are indeed signed by the replicas
        for (Map.Entry<String, SpeculativeResponse> entry : cc.getSpeculativeResponses().entrySet()) {
            if (!entry.getValue().isSignedBy(entry.getKey())) {
                log.warning("Received a commit certificate with an invalid signature");
                return false;
            }
        }

        if (cc.getSpeculativeResponses().size() < 2 * this.faultsTolerated + 1) {
            log.warning("Received a commit certificate with an incorrect number of replicas, " +
                    "got " + cc.getSpeculativeResponses().size() +
                    " expected " + (2 * this.faultsTolerated + 1));
            return false;
        }

        Set<SpeculativeResponse> speculativeResponses = new HashSet<>(cc.getSpeculativeResponses().values());
        // checks that the spec responses are all equal
        return speculativeResponses.size() <= 1;
    }

    /**
     * Forwards the receipt of a request to the primary replica
     * Corresponds to A4c in the paper
     *
     * @param clientId - the client ID
     * @param rm       - the request message
     */
    public void forwardToPrimary(String clientId, RequestMessage rm) {
        // if the request timestamp is lower or equal to the one that we have, we send the last ordered request
        if (this.getMessageLog().getMaxCC() != null && this.getMessageLog().getMaxCC().getSequenceNumber() >= rm.getTimestamp()) {
            LocalCommitMessage lcm = new LocalCommitMessage(
                    this.getViewNumber(),
                    this.getMessageLog().getMaxCC().getSpeculativeResponses().lastEntry().getValue().getReplyDigest(),
                    this.getMessageLog().getMaxCC().getSequenceNumber(),
                    this.getId(),
                    clientId
            );
            lcm.sign(this.getId());
            this.sendMessage(lcm, clientId);
        } else if (rm.getTimestamp() <= this.highestTimestamp) {
            OrderedRequestMessageWrapper ormw = this.getMessageLog().getOrderedMessages().get(this.getHighestSequenceNumber());
            this.sendMessage(ormw, this.getLeaderId());
        } else {
            long currSeqNum = this.getHighestSequenceNumber();
            ConfirmRequestMessage crm = new ConfirmRequestMessage(
                    this.getViewNumber(),
                    rm,
                    this.getId()
            );
            crm.sign(this.getId());

            this.sendMessage(crm, this.getLeaderId());
            /// TODO: additional pedantic details
            this.setTimeout(
                    "forwardToPrimary",
                    () -> {
                        if (this.getMessageLog().getOrderedMessages().containsKey(currSeqNum + 1)) {
                            return;
                        }
                        IHateThePrimaryMessage ihtpm = new IHateThePrimaryMessage(this.getViewNumber());
                        ihtpm.sign(this.getId());
                        this.broadcastMessageIncludingSelf(ihtpm);
                    },
                    // give it time in case we need to fill holes
                    Duration.ofMillis(15000)
            );
        }
    }

    private void handleProofOfMisbehaviourMessage(String sender, ProofOfMisbehaviorMessage pom) {
        if (pom.getViewNumber() != this.getViewNumber()) {
            log.warning("Received a proof of misbehaviour message with a different view number");
        } else {
            OrderedRequestMessage orm1 = pom.getPom().getLeft();
            OrderedRequestMessage orm2 = pom.getPom().getRight();
            // check for the right sequence number and that the messages are not equal
            if (orm1.getSequenceNumber() == orm2.getSequenceNumber() && !orm1.equals(orm2)) {
                // send an I hate the primary message
                IHateThePrimaryMessage ihtpm = new IHateThePrimaryMessage(this.getViewNumber());
                ihtpm.sign(this.getId());
                this.broadcastMessageIncludingSelf(ihtpm);
                this.broadcastMessage(pom);
            }
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
        if (ihtpm.getViewNumber() <= this.getViewNumber()) {
            log.warning("Received an I hate the primary message with a lower view number");
        } else if (!ihtpm.getSignedBy().equals(sender)) {
            log.warning("Received an I hate the primary message with an invalid signature");
        } else {
            this.getMessageLog().putIHateThePrimaryMessage(ihtpm);
            // if f + 1 ihatetheprimaries
            if (this.getMessageLog().getIHateThePrimaries().get(this.getViewNumber()).size() >= this.faultsTolerated + 1) {
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
        if (iHateThePrimaryMessages.size() < this.faultsTolerated + 1) {
            log.warning("Number of IHateThePrimary messages is less than f + 1");
            return;
        }
        Serializable cc;
        // creates the CCs
        if (this.getMessageLog().getMaxCC() != null && this.getMessageLog().getMaxCC().getViewNumber() == this.getViewNumber()) {
            cc = this.getMessageLog().getMaxCC();
        } else if (this.getMessageLog()
                .getViewConfirmMessages()
                .getOrDefault(this.getViewNumber(), new ArrayList<>())
                .size() >= this.faultsTolerated + 1) {
            cc = new ArrayList<>(this.getMessageLog().getViewConfirmMessages().get(this.getViewNumber()));
        } else {
            cc = this.getMessageLog().getNewViewMessages().sequencedValues().getLast();
        }

        ViewChangeMessage vcm = new ViewChangeMessage(
                this.getViewNumber() + 1,
                cc,
                this.getMessageLog().getOrderedRequestHistory(),
                this.getId()
        );
        vcm.sign(this.getId());
        ViewChangeMessageWrapper vcmw = new ViewChangeMessageWrapper(iHateThePrimaryMessages, vcm);

        if (!this.disgruntled) this.broadcastMessageIncludingSelf(vcmw);

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
        if (futureViewNumber < this.getViewNumber() + 1) {
            log.warning("Received a view change message with an incorrect view number");
            return;
        }
        // check that there are enough valid IHateThePrimary messages from the view change message
        if (!(numberIHateThePrimaries(viewChangeMessageWrapper.getIHateThePrimaries()) >= this.faultsTolerated + 1)) {
            log.warning("Received a view change message with an incorrect number of IHateThePrimary messages");
            return;
        }

        // puts the view change message in the view change messages list
        this.getMessageLog().putViewChangeMessage(vcm);

        // if we have enough view change messages, we go on to VC3
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

    private void viewChangePrimary(long newViewNumber) {
        if (this.getMessageLog().getViewChangeMessages().get(newViewNumber).size() < 2 * this.faultsTolerated + 1) {
            log.warning("Received a view change message with an incorrect number of replicas");
            return;
        }
        // create new-view message
        NewViewMessage nvm = new NewViewMessage(
                newViewNumber,
                this.getMessageLog().getViewChangeMessages().get(newViewNumber).values()
        );
        nvm.sign(this.getId());
        this.broadcastMessageIncludingSelf(nvm);
    }

    public void viewChangeReplica(long newViewNumber) {
        // we grow the timeout exponentially
        long lastViewNumber = this.getMessageLog().
                getNewViewMessages().
                get(this.getMessageLog()
                        .getNewViewMessages()
                        .lastKey())
                .getFutureViewNumber();
        long timeout = (long) Math.pow(2, (newViewNumber - lastViewNumber)) * 5000;

        this.setTimeout(
                "viewChangeReplica",
                () -> {
                    // if we have received a new-view message, then we return
                    if (this.getMessageLog().getNewViewMessages().containsKey(newViewNumber)) {
                        NewViewMessage nvm = this.getMessageLog().getNewViewMessages().get(newViewNumber);
                        // we check if the new-view message is valid
                        if (!this.isValidNewViewMessage(nvm)) {
                            return;
                        }
                        ViewConfirmMessage vcm = new ViewConfirmMessage(
                                newViewNumber,
                                this.getHighestSequenceNumber(),
                                this.getHistory().getLast(),
                                this.getId()
                        );

                        vcm.sign(this.getId());
                        this.broadcastMessageIncludingSelf(vcm);
                    }
                    // if we haven't received a new-view message, then we send a ihatetheprimary message
                    else {
                        IHateThePrimaryMessage ihtpm = new IHateThePrimaryMessage(newViewNumber + 1);
                        ihtpm.sign(this.getId());
                        this.broadcastMessageIncludingSelf(ihtpm);
                        // It's still disgruntled so we don't actually accept messages etc.
                        // But we need to update the view number for everything else
                        // For example if we receive a new-view message even though the timeout has passed
                        this.setView(newViewNumber);
                    }
                },
                Duration.ofMillis(timeout)
        );
    }

    public boolean isValidNewViewMessage(NewViewMessage nvm) {
        // check if the primary is correct
        if (!nvm.isSignedBy(this.computePrimaryId(nvm.getFutureViewNumber()))) {
            log.warning("Received a new view message with an incorrect primary");
        }
        Set<String> replicaIds = new HashSet<>();
        for (ViewChangeMessage vcm : nvm.getViewChangeMessages()) {
            if (!vcm.isSignedBy(vcm.getReplicaId())) {
                log.warning("Received a new view message with an invalid signature");
                return false;
            }
            replicaIds.add(vcm.getReplicaId());
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

        // add the view confirm message to the view confirm messages list
        this.getMessageLog().putViewConfirmMessage(vcm);

        // calculate the frequencies of the view confirm messages
        HashMap<ViewConfirmMessage, Integer> viewConfirmMessagesFrequency = new HashMap<>();
        // count the number of view confirm messages for each view number
        for (ViewConfirmMessage viewConfirmMessage : this.getMessageLog().getViewConfirmMessages().get(vcm.getFutureViewNumber())) {
            viewConfirmMessagesFrequency.put(viewConfirmMessage, viewConfirmMessagesFrequency.getOrDefault(viewConfirmMessage, 0) + 1);
        }

        // get the max frequency
        Map.Entry<ViewConfirmMessage, Integer> maxEntry = viewConfirmMessagesFrequency.entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .orElse(null);

        if (maxEntry == null) {
            log.warning("No view confirm messages found for view number " + vcm.getFutureViewNumber());
            return;
        }

        // if we have enough view confirm messages, we change the view
        if (maxEntry.getValue() >= 2 * this.faultsTolerated + 1) {
            this.handleViewChange(maxEntry.getKey());
        }
    }

    private void handleNewViewMessage(String sender, NewViewMessage nvm) {

        if (nvm.getFutureViewNumber() <= this.getViewNumber()) {
            log.warning("Received a new view message with a smaller than expected view number");
            return;
        }

        if (!nvm.isSignedBy(this.computePrimaryId(nvm.getFutureViewNumber()))) {
            log.warning("Received a new view message with an incorrect primary");
            return;
        }


        TreeSet<String> replicaIds = new TreeSet<>();

        for (ViewChangeMessage vcm : nvm.getViewChangeMessages()) {
            if (!vcm.isSignedBy(vcm.getReplicaId())) {
                log.warning("Received a new view message with an invalid signature");
                return;
            }
            replicaIds.add(vcm.getReplicaId());
        }

        if (replicaIds.size() < 2 * this.faultsTolerated + 1) {
            log.warning("Received a new view message with an insufficient number of distinct view change messages");
            return;
        }

        long lastRequest = -1L;
        int priority = 0;
        for (Serializable cc : nvm.getViewChangeMessages().stream().map(ViewChangeMessage::getCommitCertificate).toList()) {
            // we evaluate in order of Commit Certificate, then f + 1 View Confirm Messages, then the last View Change Message
            if (cc instanceof CommitCertificate commitCertificate) {
                if (!this.isValidCommitCertificate(commitCertificate)) {
                    log.warning("Received a new view message with an invalid commit certificate");
                    return;
                }
                if (priority == 3 && lastRequest < commitCertificate.getSequenceNumber()) {
                    // changes to commit certificate
                    lastRequest = commitCertificate.getSequenceNumber();
                } else if (priority < 3) {
                    lastRequest = commitCertificate.getSequenceNumber();
                    priority = 3;
                }
            } else if (cc instanceof ArrayList) {
                ArrayList<ViewConfirmMessage> castCC = (ArrayList<ViewConfirmMessage>) cc;

                if (isValidViewConfirmList(castCC)) {
                    if (priority < 2) {
                        lastRequest = castCC.getFirst().getLastKnownSequenceNumber();
                        priority = 2;
                    }
                } else {
                    log.warning("Received a new view message with an invalid view confirm list");
                    return;
                }
            }
        }
    }


    public boolean isValidViewConfirmList(ArrayList<ViewConfirmMessage> viewConfirmMessages) {

        TreeSet<String> replicaIds = new TreeSet<>();

        // check if the view confirm messages are indeed signed by the replicas
        for (ViewConfirmMessage vcm : viewConfirmMessages) {
            if (!vcm.isSignedBy(vcm.getReplicaId())) {
                log.warning("Received a new view message with an invalid signature");
                return false;
            }
            if (!replicaIds.add(vcm.getSignedBy())) {
                log.warning("Received a new view message with a duplicate replica ID");
                return false;
            }
        }
        if (replicaIds.size() < 2 * this.getFaultsTolerated() + 1) {
            log.warning("Received a new view message with an incorrect number of replicas, " +
                    "got " + replicaIds.size() +
                    " expected " + (2 * this.faultsTolerated + 1));
            return false;
        }
        return true;

    }

    /**
     * Changes the view number
     * Corresponds to VC5 in the paper
     *
     * @param vcm - The view confirm message that was received
     */
    private void handleViewChange(ViewConfirmMessage vcm) {
        // clear view specific messages
        this.getMessageLog().getIHateThePrimaries().get(this.getViewNumber()).clear();
        this.getMessageLog().getFillHoleMessages().clear();
        this.getMessageLog().getOrderedMessages().clear();
        this.getMessageLog().getSpeculativeResponses().clear();

        this.getHistory().clear();
        this.getHistory().add(vcm.getLastKnownSequenceNumber(), vcm.getHistory());

        // sets the view number and primary
        this.setView(vcm.getFutureViewNumber());

        this.setDisgruntled(false);
    }
}