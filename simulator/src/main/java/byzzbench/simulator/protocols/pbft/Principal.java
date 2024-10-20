package byzzbench.simulator.protocols.pbft;

/**
 * Principal class.
 */
public class Principal {
    /**
     * Principal identifier.
     */
    private final String id;

    /**
     * Public key.
     */
    private final String pkey;

    /**
     * Signature size.
     */
    private final int ssize;

    /**
     * last timestamp in a new-key message from this principal.
     */
    private final long tstamp;
    /**
     * Session key for outgoing messages to this principal.
     */
    private final int[] kout;
    /**
     * My timestamp.
     */
    private long my_tstamp;
    /**
     * Session key for incoming messages from this principal.
     */
    private int[] kin;
    /**
     * Last request identifier in a fetch message from this principal.
     */
    private long last_fetch;

    /**
     * Creates a new Principal object.
     * @param i Principal identifier.
     * @param pkey Public key.
     */
    public Principal(String i, String pkey) {
        this.id = i;
        this.pkey = pkey;
        this.ssize = 0;
        this.kin = new int[16];
        this.kout = new int[16];
        this.tstamp = 0;
    }

    /**
     * Returns the principal identifier.
     * @return the principal identifier.
     */
    public String pid() {
        return id;
    }

    /**
     * Returns the address of the Principal.
     * In ByzzBench, the address is the same as the id.
     * @return the address of the Principal.
     */
    public String address() {
        return id;
    }

    /**
     * Sets the session key for incoming messages, in-key, from this principal.
     * @param k Session key.
     */
    public void set_in_key(int[] k) {
        kin = k;
    }

    /**
     * Verifies the MAC of an incoming message.
     * @param src Source.
     * @param mac MAC.
     * @return true if the MAC is valid, false otherwise.
     */
    public boolean verify_mac_in(String src, String mac) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Generates a MAC for an incoming message.
     * @param src Source.
     * @param dst Destination.
     */
    public void gen_mac_in(String src, String dst) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Verifies the MAC of an outgoing message.
     * @param src Source.
     * @param mac MAC.
     * @return true if the MAC is valid, false otherwise.
     */
    public boolean verify_mac_out(String src, String mac) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Generates a MAC for an outgoing message.
     * @param src Source.
     * @param dst Destination.
     */
    public void gen_mac_out(String src, String dst) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Returns the last timestamp in a new-key message from this principal.
     * @return the last timestamp in a new-key message from this principal.
     */
    public long last_tstamp() {
        return tstamp;
    }

    /**
     * Sets the key for outgoing messages to "k" provided "t" is greater than the last value of "t" in a "set_out_key" call.
     * @param k Key.
     * @param t Timestamp.
     */
    public void set_out_key(int[] k, long t) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Returns true iff tv is less than my_tstamp.
     * @param tv Time.
     * @return true iff tv is less than my_tstamp.
     */
    public boolean is_stale(long tv) {
        return tv < my_tstamp;
    }

    /**
     * Returns the size of signatures generated by this principal.
     * @return the size of signatures generated by this principal.
     */
    public int sig_size() {
        return ssize;
    }

    /**
     * Checks a signature "sig" (from this principal) for "src_len" bytes starting at "src".
     * @param src Source.
     * @param sig Signature.
     * @param allow_self Allow self.
     * @return true if signature is valid, false otherwise.
     */
    public boolean verify_signature(String src, String sig, boolean allow_self) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Encrypts "src_len" bytes starting at "src" using this principal's public-key and places up to "dst_len" of the result in "dst".
     * @param src Source.
     * @param dst Destination.
     * @return the number of bytes placed in "dst".
     */
    public int encrypt(String src, String dst) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Gets the last request identifier in a fetch message from this principal.
     * @return the last request identifier in a fetch message from this principal.
     */
    public long last_fetch_rid() {
        return last_fetch;
    }

    /**
     * Sets the last request identifier in a fetch message from this principal.
     * @param r Request identifier.
     */
    public void set_last_fetch_rid(long r) {
        last_fetch = r;
    }
}
