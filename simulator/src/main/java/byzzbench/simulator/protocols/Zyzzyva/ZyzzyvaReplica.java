package byzzbench.simulator.protocols.Zyzzyva;

import byzzbench.simulator.LeaderBasedProtocolReplica;
import byzzbench.simulator.protocols.Zyzzyva.message.*;
import byzzbench.simulator.state.SerializableLogEntry;
import byzzbench.simulator.state.TotalOrderCommitLog;
import byzzbench.simulator.transport.MessagePayload;
import byzzbench.simulator.transport.Transport;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.java.Log;

import javax.swing.text.View;
import java.io.Serializable;
import java.util.*;

/// TODO: check if everything is signed correctly
/// TODO: checkpoints
/// TODO: when checkpointing, create a new maxCC
/// TODO: Check all fillhole logic
@Log
@ToString(callSuper = true)
public class ZyzzyvaReplica extends LeaderBasedProtocolReplica {

    @Getter
    @Setter
    private long viewNumber = -1;

    @Getter
    private final int CP_INTERVAL;

    @Getter
    private final int faultsTolerated;

    @Getter
    @Setter
    private long highestTimestamp;

    @Getter
    @Setter
    private long highestSequenceNumber;

    @Getter
    @Setter
    private boolean disgruntled = false;

    // used for the speculative history
    // we use the commit log for the committed history
    @Getter
    private final SpeculativeHistory history;

    @Getter
    @JsonIgnore
    private final MessageLog messageLog;

    public ZyzzyvaReplica(String replicaId,
                          SortedSet<String> nodeIds,
                          Transport transport,
                          int CP_INTERVAL) {
        super(replicaId, nodeIds, transport, new TotalOrderCommitLog());

        this.history = new SpeculativeHistory();
        this.messageLog = new MessageLog(15);
        this.CP_INTERVAL = CP_INTERVAL;
        this.faultsTolerated = (nodeIds.size() - 1) / 3;
    }

    @Override
    public void initialize() {
        this.setView(0);
    }

    @Override
    public void handleMessage(String sender, MessagePayload m) throws Exception {
        switch (m) {
            // done
            case RequestMessage requestMessage -> {
                if (this.disgruntled) {
                    return;
                }
                handleClientRequest(sender, requestMessage);
            }
            // done
            case OrderedRequestMessageWrapper orderedRequestMessage -> {
                if (this.disgruntled) {
                    return;
                }
                handleOrderedRequestMessageWrapper(sender, orderedRequestMessage);
            }
            // done
            case FillHoleMessage fillHoleMessage -> {
                if (this.disgruntled) {
                    return;
                }
                handleFillHoleMessageRequest(sender, fillHoleMessage);
            }
            // in progress
            case FillHoleReply fillHoleReply -> {
                handleFillHoleMessageReply(sender, fillHoleReply);
            }
            // done
            case CommitMessage commitmessage -> {
                if (this.disgruntled) {
                    return;
                }
                handleCommitMessage(sender, commitmessage);
            }
            // done
            case IHateThePrimaryMessage iHateThePrimaryMessage -> {
                handleIHateThePrimaryMessage(sender, iHateThePrimaryMessage);
            }
            // not done
            case ViewChangeMessageWrapper viewChangeMessageWrapper -> {
                handleViewChangeMessageWrapper(sender, viewChangeMessageWrapper);
            }
            // done
            case ProofOfMisbehaviorMessage proofOfMisbehaviorMessage -> {
                handleProofOfMisbehaviourMessage(sender, proofOfMisbehaviorMessage);
            }
            default -> {
                throw new RuntimeException("Unknown message type: " + m.getType());
            }
        }
    }

    /**
     * Calculate the history hash
     * @param digest - the digest of the message to add to the history
     * @return the new history hash
     */
    public long calculateHistory(long sequenceNumber, byte[] digest) {
        return (this.getHistory().get(sequenceNumber - 1) + Arrays.hashCode(digest));
    }

    /**
     * Update the history hash in the speculative history
     * @param digest - the digest of the message to add to the history
     */
    public void updateHistory(long sequenceNumber, byte[] digest) {
        this.getHistory().add(sequenceNumber, calculateHistory(sequenceNumber, digest));

    }

    /**
     * Set the view number and the primary ID
     * @param viewNumber - the view number to set
     */
    private void setView(long viewNumber) {
        this.setView(viewNumber, this.computePrimaryId(viewNumber));
    }

    /**
     * Gets the primary ID for the current view number
     * @return the primary ID
     */
    private String computePrimaryId() {
        return this.computePrimaryId(this.viewNumber);
    }

    /**
     * Computes the primary ID for a given view number
     * @param viewNumber - the view number
     * @return - the primary ID
     */
    private String computePrimaryId(long viewNumber) {
        // is this fine? it's a sorted set after all so we retain order
        return (String) this.getNodeIds().toArray()[(int) (viewNumber % this.getNodeIds().size())];
    }

    @Override
    public void handleClientRequest(String clientId, Serializable request) throws Exception {
        /// TODO: finish the additional pedantic details of 4c.
        // if this replica is the primary, then it can order the request
        if (this.computePrimaryId().equals(this.getNodeId()) && ((RequestMessage) request).getTimestamp() > this.highestTimestamp) {
            byte[] digest = this.digest(request);
            OrderedRequestMessage orm = new OrderedRequestMessage(
                    // view number
                    this.viewNumber,
                    // assign a sequence number to the request
                    this.getMessageLog().getOrderedMessages().size(),
                    // history
                    // hn = H(hn−1, d) is a digest summarizing the history
                    this.calculateHistory(this.getHighestSequenceNumber(), digest),
                    // digest
                    digest);
            orm.sign(this.getNodeId());
            OrderedRequestMessageWrapper ormw = new OrderedRequestMessageWrapper(orm, (RequestMessage) request);
            // send an ordered request message to all replicas including self
            this.broadcastMessageIncludingSelf(ormw);
        } else {
            forwardToPrimary(clientId, (RequestMessage) request);
        }
    }

    public void forwardToPrimary(String clientId, RequestMessage rm) {
        // if the request timestamp is lower or equal to the one that we have, we send the last ordered request
        if (rm.getTimestamp() <= this.highestTimestamp) {
            OrderedRequestMessageWrapper ormw = this.getMessageLog().getOrderedMessages().get(this.getHighestSequenceNumber());
            this.sendMessage(ormw, this.computePrimaryId());
        } else {
            long currSeqNum = this.getHighestSequenceNumber();
            ConfirmRequestMessage crm = new ConfirmRequestMessage(
                    this.getViewNumber(),
                    rm,
                    this.getNodeId()
            );
            crm.sign(this.getNodeId());

            this.sendMessage(crm, this.computePrimaryId());

            this.setTimeout(
                    () -> {
                        if (this.getMessageLog().getOrderedMessages().containsKey(currSeqNum + 1)) {
                            return;
                        }
                        IHateThePrimaryMessage ihtpm = new IHateThePrimaryMessage(this.viewNumber);
                        ihtpm.sign(this.getNodeId());
                        this.broadcastMessageIncludingSelf(ihtpm);
                    },
                    5000
            );
        }
    }

    public void handleConfirmRequestMessage(String sender, ConfirmRequestMessage crm) throws Exception {
        if (!this.getNodeId().equals(this.computePrimaryId())) {
            log.warning("Received confirm request message as non-primary");
            return;
        }
        if (crm.getViewNumber() != this.viewNumber) {
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
     *
     * @param sender - The sender of the message
     * @param ormw   - The ordered request message wrapper that was received
     */
    public void handleOrderedRequestMessageWrapper(String sender, OrderedRequestMessageWrapper ormw) {
        if (!this.computePrimaryId().equals(sender)) {
            log.warning("Received an ordered request message from a non-primary replica");
            return;
        }
        // check if the ordered request message is valid
        if (this.isValidOrderedRequestMessage(ormw.getOrderedRequest(), ormw.getRequestMessage())) {
            // get the client ID from the request message
            String clientId = ormw.getRequestMessage().getClientId();
            SpeculativeResponseWrapper srw = this.handleOrderedRequestMessage(ormw.getOrderedRequest(), ormw.getRequestMessage());
            // send a speculative response to the client
            sendMessage(srw, clientId);
            // saves to message log
            this.getMessageLog().getSpeculativeResponses().put(ormw.getOrderedRequest().getSequenceNumber(), srw.getSpecResponse());
            this.getMessageLog().getOrderedMessages().put(ormw.getOrderedRequest().getSequenceNumber(), ormw);
        } else {
            log.warning("Received an invalid ordered request message");
        }
    }

    public SpeculativeResponseWrapper handleOrderedRequestMessage(OrderedRequestMessage orm, RequestMessage m) {
        byte[] digest = orm.getDigest();

        this.setHighestSequenceNumber(orm.getSequenceNumber());

        this.setHighestTimestamp(m.getTimestamp());

        this.updateHistory((int) orm.getSequenceNumber(), orm.getDigest());

        SpeculativeResponse sr = new SpeculativeResponse(this.viewNumber,
                orm.getSequenceNumber(),
                this.calculateHistory(orm.getSequenceNumber(), digest),
                digest,
                m.getClientId(),
                m.getTimestamp()
        );

        sr.sign(this.getNodeId());
        return new SpeculativeResponseWrapper(sr, this.getNodeId(), m, orm);
    }

    public boolean isValidOrderedRequestMessage(OrderedRequestMessage orm, RequestMessage m) {
        // check if the view number is correct
        if (orm.getViewNumber() != this.viewNumber) {
            log.warning("Received an ordered request message with an incorrect view number");
            return false;
        }

        // check if the primary is correct
        if (!orm.isSignedBy(this.computePrimaryId())) {
            log.warning("Received an ordered request message with an incorrect primary");
            // we immediately return false because we've already checked the view number so it's guaranteed to be incorrect
            return false;
        }

        // check (n == max_n + 1)
        if (orm.getSequenceNumber() != this.getHighestSequenceNumber() + 1) {
            log.warning("Received an ordered request message with an incorrect sequence number");
            // send fillHoles
            if (orm.getSequenceNumber() > this.getHighestSequenceNumber() + 1) {
                this.fillHole(orm.getSequenceNumber());
            }
            System.out.println("Lower request number than highest sequence number");
            return false;
        }

        byte[] messageDigest = this.digest(m);

        // check if the digest is correct
        if (orm.getDigest() != messageDigest) {
            log.warning("Received an ordered request message with an incorrect digest");
            return false;
        }
        
        // check if the history hash is correct
        if (orm.getHistoryHash() != this.getHistory().getLast()) {
            log.warning("Received an ordered request message with an incorrect history hash");
            return false;
        }
        return true;
    }

    public void fillHole(long receivedSequenceNumber) {
        FillHoleMessage fhm = new FillHoleMessage(
                this.viewNumber,
                this.getHighestSequenceNumber() + 1,
                receivedSequenceNumber,
                true,
                this.getNodeId()
        );

        fhm.sign(this.getNodeId());
        this.sendMessage(fhm, this.computePrimaryId());

        this.setTimeout(
                () -> {
                    // check that we have all the responses, if we do, and they're valid, return
                    if (this.receivedFillHole(receivedSequenceNumber)) {
                        long oldSeqNum = this.getHighestSequenceNumber();
                        // add to message log
                        this.fillOrderedRequestMessages(this.computePrimaryId(), oldSeqNum + 1, receivedSequenceNumber);
                        // repair history
                        this.fillHistory(oldSeqNum + 1, receivedSequenceNumber);
                        return;
                    };
                    // send a fill hole message to all replicas
                    FillHoleMessage fhma = new FillHoleMessage(
                            this.viewNumber,
                            this.getHighestSequenceNumber() + 1,
                            receivedSequenceNumber,
                            false,
                            this.getNodeId());
                    fhma.sign(this.getNodeId());
                    this.broadcastMessage(fhma);
                    this.setTimeout(
                            () -> {
                                // check that we have all the responses
                                // if not, we send a proof of misbehaviour
                                // if yes, then we add to message log and repair history
                                // we don't care about it being "correct" since we're going to sync up eventually

                            },
                            5000
                    );
                },
                10000);
    }

    /**
     * Fills the ordered request message log from the fill hole messages once found valid
     * @param senderId - the sender of the fill hole replies
     * @param lowSeqNum - the first sequence number to fill (inclusive)
     * @param highSeqNum - the last sequence number to fill (inclusive
     */
    private void fillOrderedRequestMessages(String senderId, long lowSeqNum, long highSeqNum) {
        // add to message logs
        for (long i = lowSeqNum; i <= highSeqNum; i++) {
            FillHoleReply fhr = this.getMessageLog().getFillHoleMessages().get(i).values().iterator().next();
            OrderedRequestMessageWrapper ormw = new OrderedRequestMessageWrapper(
                    fhr.getOrderedRequestMessage(),
                    fhr.getRequestMessage()
            );
            this.getMessageLog().getOrderedMessages().put(i, ormw);
        }
    }

    /**
     * Fills the history from the fill hole messages once found valid
     * @param seqLow - the first sequence number to fill (inclusive)
     * @param seqHigh - the last sequence number to fill (inclusive)
     */
    public void fillHistory(long seqLow, long seqHigh) {
        for (long i = seqLow; i < seqHigh; i++) {
            byte[] digest = this.digest(this.getMessageLog().getOrderedMessages().get(i).getRequestMessage());
            this.updateHistory(i, digest);
        }
    }

    /**
     * Handles the fill hole message request by validating and calling sendFillHoleReplies().
     *
     * @param sender - The sender of the fill hole message
     * @param fillHoleMessage - The fill hole message that was received
     */
    private void handleFillHoleMessageRequest(String sender, FillHoleMessage fillHoleMessage) {
        // for the primary
        if (fillHoleMessage.isMeantForPrimary() && this.getNodeId().equals(this.computePrimaryId())) {
            if (this.getViewNumber() != fillHoleMessage.getViewNumber()) {
                log.warning("Received a fill hole message with a different view number");
            } else {
                sendFillHoleReplies(fillHoleMessage.getReplicaId(), fillHoleMessage.getLastKnownSequenceNumber(), fillHoleMessage.getReceivedSequenceNumber());
            }
        
        } else if (fillHoleMessage.isMeantForPrimary()) {
            log.warning("Received a fill hole message that was meant for the primary");
        } else {
            sendFillHoleReplies(fillHoleMessage.getReplicaId(), fillHoleMessage.getLastKnownSequenceNumber(), fillHoleMessage.getReceivedSequenceNumber());
        }
    }

    /**
     * Sends the fill hole replies to the replica that requested them.
     * @param sender - The sender of the fill hole message
     * @param lastKnownSequenceNumber - The last sequence number of the replica (inclusive)
     * @param receivedSequenceNumber - The received sequence number of the replica (inclusive)
     */
    private void sendFillHoleReplies(String sender, long lastKnownSequenceNumber, long receivedSequenceNumber) {
        for (long n = lastKnownSequenceNumber; n <= receivedSequenceNumber; n++) {
            try {
                // try sending if it exists
                OrderedRequestMessageWrapper ormw = this.getMessageLog().getOrderedMessages().get(n);
                this.sendMessage(ormw, sender);
            } catch (NullPointerException e) {
                log.warning("Replica " + this.getNodeId() + " does not have the message with sequence number " + n);
            }
        }
    }

    /**
     * Handles the fill hole message reply by adding the message to the message log.
     * @param sender - The sender of the fill hole reply
     * @param fillHoleReply - The fill hole reply that was received
     */
    private void handleFillHoleMessageReply(String sender, FillHoleReply fillHoleReply) {
        /// TODO: Check if we need to add the spec response to the speculative response history
        if (fillHoleReply.getOrderedRequestMessage().getViewNumber() != this.viewNumber) {
            log.warning("Received a fill hole reply with a different view number");
        } else {
            // add to the fillHoleReply map in the message log
            this.getMessageLog().getFillHoleMessages().get(fillHoleReply.getOrderedRequestMessage().getSequenceNumber()).put(sender, fillHoleReply);
        }
    }

    private boolean receivedFillHole(long receivedSequenceNumber) {
        // the last history that we have
        long prevHistory = this.getHistory().get(this.getHighestSequenceNumber());
        // check that all fill holes have been received
        for (long i = this.getHighestSequenceNumber() + 1; i < receivedSequenceNumber; i++) {
            // if we miss messages, return false
            if (!this.getMessageLog().getFillHoleMessages().containsKey(i)) {
                return false;
            }
            /// TODO: ask if we need this block? How would the primary lie since the next request sent to everyone has to be correct
            else {
                // check if the message is valid
                FillHoleReply current = this.getMessageLog().getFillHoleMessages().get(i).get(this.computePrimaryId());
                byte[] calcDigest = this.digest(current.getRequestMessage());
                // check digests
                if (current.getOrderedRequestMessage().getDigest() != calcDigest) {
                    return false;
                }
                // calculate new history
                long newHistory = prevHistory + Arrays.hashCode(calcDigest);
                // check history hashes
                if (current.getOrderedRequestMessage().getHistoryHash() != newHistory) {
                    log.warning(
                            "Received a fill hole reply with an incorrect history hash:" +
                            current.getOrderedRequestMessage().getHistoryHash() +
                            " " + newHistory
                    );
                    return false;
                }
                prevHistory = newHistory;
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

        // We don't trust the client, so we check if everything matches
        SpeculativeResponse firstResponse = cc.getSpeculativeResponses().getFirst();
        // checks if the speculative responses are equal
        for (SpeculativeResponse sr : cc.getSpeculativeResponses()) {
            if (!firstResponse.equals(sr)) {
                log.warning("Received a commit certificate with unequal speculative responses");
                return;
            }
        }
        // check if the history is consistent with the one certified by CC
        if (firstResponse.getHistory() != this.getHistory().get(firstResponse.getSequenceNumber())) {
            log.warning("Received a commit certificate with an inconsistent history");
            if (cc.getSequenceNumber() > this.getHighestSequenceNumber() + 1) {
                this.fillHole(firstResponse.getSequenceNumber());
            } else {
                // initiate a view change
                IHateThePrimaryMessage ihtpm = new IHateThePrimaryMessage(this.viewNumber);
                ihtpm.sign(this.getNodeId());
                this.broadcastMessageIncludingSelf(ihtpm);
            }
        } else {
            // if the history is consistent, then we commit the operation
            this.handleCommitCertificate(cc);
            /// TODO: check if it's actually client request digest and not reply digest
            LocalCommitMessage lcm = new LocalCommitMessage(this.getViewNumber(),
                    firstResponse.getReplyDigest(),
                    this.getHistory().getLast(),
                    this.getNodeId(),
                    cc.getClientId());
            lcm.sign(this.getNodeId());
            this.sendMessage(lcm, cc.getClientId());
        }
    }

    private void handleCommitCertificate(CommitCertificate cc) {
        CommitCertificate currentCC = this.getMessageLog().getMaxCC();
        if (currentCC == null || cc.getSequenceNumber() > currentCC.getSequenceNumber()) {
            long oldSeqNum = (currentCC != null) ? currentCC.getSequenceNumber() + 1 : 0;
            for (long i = oldSeqNum; i < cc.getSequenceNumber(); i++) {
                this.commitOperation(new SerializableLogEntry(this.getMessageLog().getOrderedMessages().get(i).getRequestMessage().getOperation()));
            }
            this.getMessageLog().setMaxCC(cc);
        }
    }

    /**
     * Checks if the commit certificate has the right size and the speculative responses match
     * @param cc - the commit certificate to check
     * @return - true if the commit certificate is valid, false otherwise
     */
    private boolean isValidCommitCertificate(CommitCertificate cc) {
        if (cc.getSpeculativeResponses().isEmpty()) {
            log.warning("Received a commit certificate with no replicas");
            return false;
        }

        if (cc.getReplicaIds().size() < 2 * this.faultsTolerated + 1) {
            log.warning("Received a commit certificate with an incorrect number of replicas, " +
                    "got " + cc.getReplicaIds().size() +
                    " expected " + (2 * this.faultsTolerated + 1));
            return false;
        }

        SpeculativeResponse firstResponse = cc.getSpeculativeResponses().getFirst();
        // check if the speculative responses are equal
        for (int i = 0; i < cc.getReplicaIds().size(); i++) {
            String curr_replica_id = cc.getReplicaIds().get(i);
            SpeculativeResponse sr = cc.getSpeculativeResponses().get(i);
            if (!sr.isSignedBy(curr_replica_id)) {
                log.warning("Received a commit certificate with an invalid signature");
                return false;
            } else if (!firstResponse.equals(sr)) {
                log.warning("Received a commit certificate with unequal speculative responses");
                return false;
            }
        }
        return true;
    }

    private void handleProofOfMisbehaviourMessage(String sender, ProofOfMisbehaviorMessage pom) {
        if (pom.getViewNumber() != this.viewNumber) {
            log.warning("Received a proof of misbehaviour message with a different view number");
        } else {
            OrderedRequestMessage orm1 = pom.getPom().getLeft();
            OrderedRequestMessage orm2 = pom.getPom().getRight();
            // check for the right sequence number and that the messages are not equal
            if (orm1.getSequenceNumber() == orm2.getSequenceNumber() && !orm1.equals(orm2)) {
                // send an I hate the primary message
                IHateThePrimaryMessage ihtpm = new IHateThePrimaryMessage(this.viewNumber);
                ihtpm.sign(this.getNodeId());
                this.broadcastMessageIncludingSelf(ihtpm);
                this.broadcastMessage(pom);
            }
        }
    }

    /**
     * Handles the I hate the primary message received by this replica by adding it to the message log
     * Also initiates a view change if f + 1 IHateThePrimary messages are received
     * Corresponds to VC1 in the paper
     * @param sender - The sender of the message
     * @param ihtpm - The I hate the primary message that was received
     */
    private void handleIHateThePrimaryMessage(String sender, IHateThePrimaryMessage ihtpm) {
        if (ihtpm.getViewNumber() < this.viewNumber) {
            log.warning("Received an I hate the primary message with a different view number");
        } else if (!ihtpm.getSignedBy().equals(sender)) {
            log.warning("Received an I hate the primary message with an invalid signature");
        } else {
            this.getMessageLog().getIHateThePrimaries().get(ihtpm.getViewNumber()).put(ihtpm.getSignedBy(), ihtpm);
            // if f + 1 ihatetheprimaries
            if (this.getMessageLog().getIHateThePrimaries().size() > this.faultsTolerated) {
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
        Serializable cc;
        // creates the CCs
        if (this.getMessageLog().getMaxCC() == null) {
            cc = this.getMessageLog().getMaxCC();
        }
        else if (this.getMessageLog()
                .getViewConfirmMessages()
                .getOrDefault(this.viewNumber, new ArrayList<>())
                .size() > this.faultsTolerated) {
            cc = new ArrayList<>(this.getMessageLog().getViewConfirmMessages().get(this.viewNumber));
        } else {
            cc = this.getMessageLog().getNewViewMessages().sequencedKeySet().getLast();
        }

        ViewChangeMessage vcm = new ViewChangeMessage(
                this.viewNumber + 1,
                cc,
                this.getMessageLog().getOrderedRequestHistory(),
                this.getNodeId()
        );
        vcm.sign(this.getNodeId());
        ViewChangeMessageWrapper vcmw = new ViewChangeMessageWrapper(iHateThePrimaryMessages, vcm);
        this.broadcastMessageIncludingSelf(vcmw);

        this.setDisgruntled(true);
    }

    /**
     * Handles the view change message wrapper received by this replica
     * Corresponds to the reception of VC3 in the paper
     * @param sender - The sender of the message
     * @param viewChangeMessageWrapper - The view change message wrapper that was received
     */
    private void handleViewChangeMessageWrapper(String sender, ViewChangeMessageWrapper viewChangeMessageWrapper) {
        ViewChangeMessage vcm = viewChangeMessageWrapper.getViewChangeMessage();
        long viewNumber = vcm.getViewNumber();
        // discard previous views
        if (viewNumber < this.viewNumber + 1) {
            log.warning("Received a view change message with an incorrect view number");
            return;
        }
        // check that there are enough valid IHateThePrimary messages from the view change message
        if (!(numberIHateThePrimaries(viewChangeMessageWrapper.getIHateThePrimaries()) > this.faultsTolerated)) {
            log.warning("Received a view change message with an incorrect number of IHateThePrimary messages");
            return;
        }

        // puts the view change message in the view change messages list
        this.getMessageLog().getViewChangeMessages().get(viewNumber).put(vcm.getReplicaId(), vcm);

        // if we have enough view change messages, we go on to VC3
        // note: this can be for any view number > current view number, so v + 100 for example is also valid here
        if (this.getMessageLog()
                .getViewChangeMessages()
                .get(vcm.getViewNumber())
                .size() >
                (2 * this.faultsTolerated + 1)) {
            // go on to VC3
            this.viewChange(vcm.getViewNumber());
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
        if (this.computePrimaryId(newViewNumber).equals(this.getNodeId())) {
            this.viewChangePrimary(newViewNumber);
        } else {
            this.viewChangeReplica(newViewNumber);
        }
    }

    public void viewChangePrimary(long newViewNumber) {
        // create new-view message
        NewViewMessage nvm = new NewViewMessage(
                newViewNumber,
                this.getMessageLog().getViewChangeMessages().get(newViewNumber).values()
        );
        nvm.sign(this.getNodeId());
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
                () -> {
                    // if we have received a new-view message, then we return
                    if (this.getMessageLog().getNewViewMessages().containsKey(newViewNumber)) {
                        NewViewMessage nvm = this.getMessageLog().getNewViewMessages().get(newViewNumber);
                        // we check if the new-view message is valid
                        if(!this.isValidNewViewMessage(nvm)) {
                            return;
                        }
                        ViewConfirmMessage vcm = new ViewConfirmMessage(
                                newViewNumber,
                                this.getHighestSequenceNumber(),
                                this.getHistory().getLast(),
                                this.getNodeId()
                        );

                        vcm.sign(this.getNodeId());
                        this.broadcastMessageIncludingSelf(vcm);
                    }
                    // if we haven't received a new-view message, then we send a ihatetheprimary message
                    else {
                        IHateThePrimaryMessage ihtpm = new IHateThePrimaryMessage(newViewNumber + 1);
                        ihtpm.sign(this.getNodeId());
                        this.broadcastMessageIncludingSelf(ihtpm);
                        // It's still disgruntled so we don't actually accept messages etc.
                        // But we need to update the view number for everything else
                        // For example if we receive a new-view message even though the timeout has passed
                        this.setView(newViewNumber);
                    }
                },
                timeout
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
     * @param sender - The sender of the message
     * @param vcm - The view confirm message that was received
     */
    public void handleViewConfirmMessage(String sender, ViewConfirmMessage vcm) {
        // check if the vcm is signed by the sender
        if (!vcm.isSignedBy(sender)) {
            log.warning("Received a view confirm message with an invalid signature");
            return;
        }

        this.getMessageLog().getViewConfirmMessages().get(vcm.getFutureViewNumber()).add(vcm);
        // calculate the frequencies of the view confirm messages
        HashMap<ViewConfirmMessage, Integer> viewConfirmMessagesFrequency = new HashMap<>();
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
        if (maxEntry.getValue() > 2 * this.faultsTolerated + 1) {
            this.handleViewChange(maxEntry.getKey());
        }
    }

    /**
     * Changes the view number and creates a new commit certificate
     * Corresponds to VC5 in the paper
     * @param vcm - The view confirm message that was received
     */
    private void handleViewChange(ViewConfirmMessage vcm) {
        // clear view specific messages
        this.getMessageLog().getIHateThePrimaries().get(this.getViewNumber()).clear();
        this.getMessageLog().getFillHoleMessages().clear();
        this.getMessageLog().getOrderedMessages().clear();



        this.setViewNumber(vcm.getFutureViewNumber());
    }
}