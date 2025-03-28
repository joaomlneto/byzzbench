package byzzbench.simulator.protocols.pbft_java;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.protocols.pbft_java.message.PrePrepareMessage;
import byzzbench.simulator.protocols.pbft_java.message.PrepareMessage;
import byzzbench.simulator.protocols.pbft_java.message.RequestMessage;
import lombok.extern.java.Log;

import java.io.Serializable;
import java.time.Duration;
import java.util.Arrays;

@Log
public class FixedPbftJavaReplica<O extends Serializable, R extends Serializable> extends PbftJavaReplica<O, R> {
    public FixedPbftJavaReplica(String replicaId, int tolerance, Duration timeout, MessageLog messageLog, Scenario scenario) {
        super(replicaId, scenario, tolerance, timeout, messageLog);
    }

    @Override
    public void recvPrePrepare(PrePrepareMessage prePrepare) {
        if (!this.verifyPhaseMessage(prePrepare)) {
            return;
        }

        long currentViewNumber = this.getViewNumber();
        byte[] digest = prePrepare.getDigest();
        RequestMessage request = prePrepare.getRequest();
        long seqNumber = prePrepare.getSequenceNumber();

        // PBFT 4.2 - Verify request digest
        byte[] computedDigest = this.digest(request);
        if (!Arrays.equals(digest, computedDigest)) {
            return;
        }

        /*
         * PBFT 4.2 specifies that given a valid PRE-PREPARE (matching
         * signature, view and valid sequence number), the replica must only
         * accept a PRE-PREPARE given that no other PRE-PREPARE has been
         * received OR that the new PRE-PREPARE matches the digest of the one
         * that was already received.
         *
         * Upon accepting the PRE-PREPARE, the replica adds it to the log and
         * multicasts a PREPARE to all other replicas and adding the PREPARE to
         * its log.
         */
        Ticket<O, R> ticket = messageLog.getTicket(currentViewNumber, seqNumber);
        if (ticket != null) {
            // PRE-PREPARE has previously been inserted into the log for this
            // sequence number - verify the digests match per PBFT 4.2
            for (Object message : ticket.getMessages()) {
                if (!(message instanceof PrePrepareMessage prevPrePrepare)) {
                    continue;
                }

                byte[] prevDigest = prevPrePrepare.getDigest();
                if (!Arrays.equals(prevDigest, digest)) {
                    return;
                }
            }
        } else {
            // PRE-PREPARE is the first - create a new ticket for it in this
            // replica (see #recvRequest(ReplicaRequest, boolean) for why the
            // message log is structured this way)
            ticket = messageLog.newTicket(currentViewNumber, seqNumber);
        }

        // PBFT 4.2 - Add PRE-PREPARE along with its REQUEST to the log
        ticket.append(prePrepare);

        // PBFT 4.2 - Multicast PREPARE to other replicas
        PrepareMessage prepare = new PrepareMessage(
                currentViewNumber,
                seqNumber,
                digest,
                this.getId());
        this.broadcastMessage(prepare);

        // PBFT 4.2 - Add PREPARE to the log
        ticket.append(prepare);

        /*
         * Per PBFT 4.2, this replica stasfies the prepared predicate IF it has
         * valid PRE-PREPARE, REQUEST and PREPARE messages. Since processing is
         * done asynchronously, the replica state is checked when PRE-PREPARE
         * is accepted in case it arrives later than the corresponding PREPARE
         * messages.
         */
        this.tryAdvanceState(ticket, prePrepare);
    }
}
