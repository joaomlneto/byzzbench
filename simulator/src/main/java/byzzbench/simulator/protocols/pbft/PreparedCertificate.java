package byzzbench.simulator.protocols.pbft;

import byzzbench.simulator.protocols.pbft.message.PrePrepareMessage;
import byzzbench.simulator.protocols.pbft.message.PrepareMessage;

import java.time.Instant;
import java.util.BitSet;
import java.util.Optional;

public class PreparedCertificate extends Certificate<CertifiableMessage> implements SeqNumLog.SeqNumLogEntry {
    /**
     * Prepare certificate
     */
    private final Certificate<PrepareMessage> pc;
    /**
     * Pre-prepare info
     */
    PrePrepareInfo pi;
    /**
     * Time at which pp was sent (if I am primary)
     */
    Instant t_sent;
    /**
     * True iff pp was added with add_mine
     */
    boolean primary;

    /**
     * Creates an empty prepared certificate.
     */
    public PreparedCertificate(PbftReplica replica) {
        super(replica);
        this.pc = new Certificate<>(replica, replica.f() * 2);
    }

    /**
     * Adds {@link PrepareMessage} "m" to the certificate and returns true provided "m" satisfies:
     * 1. there is no message from "m.id()" in the certificate
     * 2. "m->verify() == true"
     * 3. if "pc.cvalue() != 0", "pc.cvalue()->match(m)";
     * otherwise, it has no effect on this and returns false.
     * This becomes the owner of "m" (i.e., no other code should delete "m" or
     * retain pointers to "m".)
     *
     * @param m the Prepare message to add
     * @return true if the message was added, false otherwise
     */
    public boolean add(PrepareMessage m) {
        return pc.add(m);
    }

    /**
     * Adds {@link PrePrepareMessage} "m" to the certificate and returns true provided "m" satisfies:
     * 1. there is no prepare from the calling principal in the certificate
     * 2. "m->verify() == true"
     *
     * @param m the PrePrepare message to add
     * @return true if the message was added, false otherwise
     */
    public boolean add(PrePrepareMessage m) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * If "cvalue() != 0" and "!cvalue().match(m)", it has no effect and returns false.
     * Otherwise, adds "m" to the certificate and returns. This becomes the owner of "m".
     * Requires the identifier of the calling principal is "m.id()", it is not the
     * primary for "m->view()", and "mine()==0".
     *
     * @param m the message to add
     * @return true if the message was added, false otherwise
     */
    public boolean add_mine(PrepareMessage m) {
        if (!(this.replica.id().equals(this.replica.primary(m.view())))) {
            throw new IllegalArgumentException("Invalid argument");
        }

        return pc.add_mine(m);
    }

    /**
     * Adds "m" to the certificate and returns true.
     * Requires the identifier of the calling principal is "m.id()", it is the
     * primary for "m->view()", and it has no message in certificate.
     *
     * @param m the message to add
     * @return true if the message was added, false otherwise
     */
    public boolean add_mine(PrePrepareMessage m) {
        if (!(this.replica.id().equals(this.replica.primary(m.view())))) {
            throw new IllegalArgumentException("Invalid argument");
        }

        if (pi.pre_prepare().isPresent()) {
            throw new IllegalStateException("Invalid state");
        }

        this.pi.add_complete(m);
        this.primary = true;
        this.t_sent = this.replica.getCurrentTime();
        return true;
    }

    /**
     * Adds a pre-prepare message matching this from an old view.
     * Requires there is no pre-prepare in this.
     *
     * @param m the message to add
     */
    public void add_old(PrePrepareMessage m) {
        if (pi.pre_prepare().isPresent()) {
            throw new IllegalStateException("Invalid state");
        }
        pi.add(m);
    }

    /**
     * If there is a pre-prepare message in this whose i-th reference to a
     * big request is d, records that d is cached and may make the
     * certificate complete.
     *
     * @param d the digest of the big request
     * @param i the index of the reference
     */
    public void add(Digest d, long i) {
        this.pi.add(d, i);
    }

    /**
     * If the calling replica has a prepare message in the certificate, returns it
     * and sets "*t" (if not null) to point to the time at which the message was
     * last sent. Otherwise, it has no effect and returns null.
     *
     * @return the prepare message in the certificate if it exists, null otherwise.
     */
    public MessageWithTime<PrepareMessage> my_prepare() {
        return pc.mine();
    }

    /**
     * If the calling replica has a pre-prepare message in the certificate, returns it
     * and sets "*t" (if not null) to point to the time at which the message was
     * last sent. Otherwise, it has no effect and returns null.
     *
     * @return the pre-prepare message in the certificate if it exists, null otherwise.
     */
    public MessageWithTime<PrePrepareMessage> my_pre_prepare() {
        if (primary) {
            Optional<PrePrepareMessage> ppm = pi.pre_prepare();
            return new MessageWithTime<>(ppm, t_sent);
        }
        return null;
    }

    /**
     * Returns number of prepares in certificate that are known to be correct.
     *
     * @return number of prepares in certificate that are known to be correct
     */
    public int num_correct() {
        return pc.num_correct();
    }

    /**
     * Checks if the pre-prepare-info is complete
     *
     * @return true iff the pre-prepare-info is complete, false otherwise
     */
    public boolean is_pp_complete() {
        return pi.is_complete();
    }

    /**
     * Checks if there are f prepares with same digest as pre-prepare
     *
     * @return true iff there are f prepares with same digest as pre-prepare, false otherwise.
     */
    public boolean is_pp_correct() {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Returns the pre-prepare in the certificate (or null if there is none)
     *
     * @return the pre-prepare in the certificate (or null if there is none)
     */
    public Optional<PrePrepareMessage> pre_prepare() {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Return a bit map with a bit reset for each request that is missing in pre-prepare
     */
    public BitSet missing_reqs() {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Returns the pre-prepare in the certificate and removes it
     *
     * @return the pre-prepare in the certificate
     */
    public PrePrepareMessage rem_pre_prepare() {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * If there is a correct prepare value, returns it. Otherwise, returns null.
     *
     * @return the correct prepare value if it exists, null otherwise
     */
    public Optional<PrepareMessage> prepare() {
        throw new UnsupportedOperationException("Not implemented");
    }


}
