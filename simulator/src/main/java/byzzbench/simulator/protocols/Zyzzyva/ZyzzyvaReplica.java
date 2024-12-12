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

import java.io.Serializable;
import java.util.*;

/// TODO: check if everything is signed correctly
/// TODO: checkpoints
@Log
@ToString(callSuper = true)
public class ZyzzyvaReplica extends LeaderBasedProtocolReplica {

    @Getter
    @Setter
    private long viewNumber = -1;

    @Getter
    private final int CP_INTERVAL;

    @Getter
    private int faultsTolerated;

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
            // in progress
            case CommitMessage commitmessage -> {
                if (this.disgruntled) {
                    return;
                }
                handleCommitMessage(sender, commitmessage);
            }
            case IHateThePrimaryMessage iHateThePrimaryMessage -> {
                handleIHateThePrimaryMessage(sender, iHateThePrimaryMessage);
            }
            // not done
            case ViewChangeMessageWrapper viewChangeMessageWrapper -> {
                handleViewChangeMessageWrapper(sender, viewChangeMessageWrapper);
            }

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
    public void updateHistory(int sequenceNumber, byte[] digest) {
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
        // if this replica is the primary, then it can order the request
        if (this.computePrimaryId().equals(this.getNodeId()) && ((RequestMessage) request).getTimestamp() > this.highestTimestamp) {
            byte[] digest = this.digest(request);
            /// TODO: Ask if this is the correct way to hash the history
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
            /// TODO: change this to be the protocol in 4c
            // if this replica is not the primary, then it creates the confirm request message
            // starts a timer and then sends the message to the primary
            ConfirmRequestMessage crm = new ConfirmRequestMessage(
                    // view number
                    this.viewNumber,
                    // request
                    (RequestMessage) request,
                    // replica id
                    this.getNodeId()
            );

            this.sendMessage(crm, this.computePrimaryId());

            final long sequenceNumber = this.getHighestSequenceNumber() + 1;

            this.setTimeout(
                    () -> {
                        // if the primary does not respond in time, then the replica sends a view change message
                        if (this.getMessageLog().getOrderedMessages().containsKey(sequenceNumber)) {
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
            // send a speculative response to the client
            sendMessage(this.handleOrderedRequestMessage(ormw.getOrderedRequest(), ormw.getRequestMessage()), clientId);
            // saves to message log
            this.getMessageLog().getOrderedMessages().put(ormw.getOrderedRequest().getSequenceNumber(), ormw);
        } else {
            log.warning("Received an invalid ordered request message");
        }
    }

    public SpeculativeResponseWrapper handleOrderedRequestMessage(OrderedRequestMessage orm, RequestMessage m) {
        byte[] digest = orm.getDigest();

        this.setHighestSequenceNumber(orm.getSequenceNumber());

        /// TODO: check if we need to check the highest timestamp before we do this?
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
            /// TODO: send a state-request message that receives the latest checkpoint as well as the order requests or commit certificates to fill in the gaps
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
                this.getNodeId());

        fhm.sign(this.getNodeId());

        this.sendMessage(fhm, this.computePrimaryId());
        this.setTimeout(
                () -> {
                    // check that we have all the responses, if we do and they're valid, return
                    if (this.checkValidPrimaryFillHole(receivedSequenceNumber)) return;
                    // send a fill hole message to all replicas
                    FillHoleMessage fhma = new FillHoleMessage(
                            this.viewNumber,
                            this.getHighestSequenceNumber() + 1,
                            receivedSequenceNumber,
                            false,
                            this.getNodeId());
                    this.broadcastMessage(fhma);
                },
                5000);
    }


    private void handleFillHoleMessageRequest(String sender, FillHoleMessage fillHoleMessage) {
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

    private void handleFillHoleMessageReply(String sender, FillHoleReply fillHoleReply) {
        if (fillHoleReply.getOrderedRequestMessage().getViewNumber() != this.viewNumber) {
            log.warning("Received a fill hole reply with a different view number");
        } else {
            if (this.computePrimaryId().equals(sender)) {
                OrderedRequestMessageWrapper ormw = new OrderedRequestMessageWrapper(
                        fillHoleReply.getOrderedRequestMessage(),
                        fillHoleReply.getRequestMessage()
                );
                // add the message to the message log
                this.getMessageLog().getOrderedMessages().put(fillHoleReply.getOrderedRequestMessage().getSequenceNumber(), ormw);
            }
        }
    }

    private boolean checkValidPrimaryFillHole(long receivedSequenceNumber) {
        long prevHistory = this.getHistory().get(this.getHighestSequenceNumber());
        for (long i = this.getHighestSequenceNumber() + 1; i < receivedSequenceNumber; i++) {
            // if we miss messages, return false
            if (!this.getMessageLog().getFillHoleMessages().get(i).containsKey(this.computePrimaryId())) {
                return false;
            } else {
                // check if the message is valid
                FillHoleReply current = this.getMessageLog().getFillHoleMessages().get(i).get(this.computePrimaryId());
                byte[] calcDigest = this.digest(current.getRequestMessage());
                // check digests
                if (current.getOrderedRequestMessage().getDigest() != calcDigest) {
                    return false;
                }
                // calculate histories
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
        if (!this.isValidCommitCertificate(commitMessage.getCommitCertificate())) {
            log.warning("Received an invalid commit message");
            return;
        }
        CommitCertificate cc = commitMessage.getCommitCertificate();
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
        if (firstResponse.getHistory() != this.getHistory().getLast()) {
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
            CommitCertificate currentCC = this.getMessageLog().getMaxCC();
            if (currentCC == null || cc.getSequenceNumber() > currentCC.getSequenceNumber()) {
                this.getMessageLog().setMaxCC(cc);
            }
        }
        /// TODO: add the operations to the commit log

    }

    // done
    private boolean isValidCommitCertificate(CommitCertificate cc) {
        if (cc.getSpeculativeResponses().isEmpty()) {
            log.warning("Received a commit certificate with no replicas");
            return false;
        }

        if (cc.getReplicaIds().size() != this.getNodeIds().size()) {
            log.warning("Received a commit certificate with an incorrect number of replicas");
            return false;
        }

        SpeculativeResponse firstResponse = cc.getSpeculativeResponses().getFirst();

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
    private void handleIHateThePrimaryMessage(String sender, IHateThePrimaryMessage iHateThePrimaryMessage) {
        if (iHateThePrimaryMessage.getViewNumber() != this.viewNumber) {
            log.warning("Received an I hate the primary message with a different view number");
        } else {
            this.getMessageLog().getIHateThePrimaries().put(sender, iHateThePrimaryMessage);
            if (this.getMessageLog().getIHateThePrimaries().size() > this.faultsTolerated) {
                this.initialiseViewChange(this.getViewNumber());
            }
        }
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

    private void initialiseViewChange(long currentViewNumber) {
        List<IHateThePrimaryMessage> iHateThePrimaryMessages = List.copyOf(this.getMessageLog().getIHateThePrimaries().values());
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
            /// TODO: ask about how you can have a new-view message
            // it's the message from the last view maybe?
            /// TODO: Delete this and replace
            cc = this.getMessageLog().getMaxCC();
        }

        // if there is a maxCC, then we send a view change message with the maxCC
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

    private void handleViewChangeMessageWrapper(String sender, ViewChangeMessageWrapper viewChangeMessageWrapper) {
        /// TODO: make sure that it belongs to the same view because otherwise we could receive out of order view change messages
        ViewChangeMessage vcm = viewChangeMessageWrapper.getViewChangeMessage();
        long viewNumber = vcm.getViewNumber();
        if (viewNumber != this.viewNumber + 1) {
            log.warning("Received a view change message with an incorrect view number");
            return;
        }
        // puts the view change message in the view change messages list
        this.getMessageLog().getViewChangeMessages().get(viewNumber).put(vcm.getReplicaId(), vcm);
        // if we have enough view change messages, then we send a new view message
        if (this.getMessageLog()
                .getViewChangeMessages()
                .get(vcm.getViewNumber())
                .size() >
                (2 * this.faultsTolerated + 1)) {
            this.viewChange(vcm.getViewNumber());
        }
    }

    /**
     * Corresponds to VC3 in the paper
     */

    private void viewChange(long viewNumber) {
        // primary logic
        if (this.computePrimaryId(viewNumber).equals(this.getNodeId())) {
            // create new-view message
            NewViewMessage nvm = new NewViewMessage(
                    viewNumber,
                    this.getMessageLog().getViewChangeMessages().get(viewNumber).values()
            );
            nvm.sign(this.getNodeId());
            this.broadcastMessageIncludingSelf(nvm);
        } else {
            // non-primary logic
            // start timer
            long timeout = 5000;
            // we grow the timeout exponentially
            // get the latest view number from the viewMessage
            /// TODO: check the logic, we initiate a view change by sending a ihatetheprimary
            long lastViewNumber = this.getMessageLog().
                    getNewViewMessages().
                    get(this.getMessageLog()
                            .getNewViewMessages()
                            .lastKey())
                    .getFutureViewNumber();
            timeout = (long) Math.pow(2, (viewNumber - lastViewNumber)) * timeout;
            this.setTimeout(
                    () -> {
                        // if we have received a new-view message, then we return
                        if (this.getMessageLog().getNewViewMessages().containsKey(viewNumber)) {
                            return;
                        }
                        // if we have not received a new-view message, then we send a ihatetheprimary message
                        // we can check using the message log if we haven't received a new-view message for the last rounds
                        /// TODO: implement logic for setting timeout and new-view message

                    },
                    timeout
            );

        }

    }

    private void handleViewChange() {
        /// TODO: deal with the message log, delete the ihatetheprimaries
        this.setViewNumber(this.getViewNumber() + 1);

    }

    /// TODO: Repair the history after receiving fillholes

}
