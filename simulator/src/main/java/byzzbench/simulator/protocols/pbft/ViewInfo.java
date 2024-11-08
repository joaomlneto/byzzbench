package byzzbench.simulator.protocols.pbft;

import byzzbench.simulator.protocols.pbft.message.*;
import java.time.Instant;
import java.util.*;
import lombok.Data;

/**
 * Holds information for the PBFT view-change protocol
 */
public class ViewInfo {
  /**
   * The parent replica object
   */
  private final PbftReplica replica;

  /**
   * View number (v)
   */
  private final long v;
  /**
   * View-change messages with the highest view number "vn" for each
   * replica such that "vn >= v" and there is no new-view message for
   * "vn". It is indexed by replica ID.
   */
  private final SortedMap<String, ViewChangeMessage> last_vcs = new TreeMap<>();
  /**
   * Highest view numbers in view-changes received from each replica.
   * It is indexed by replica id.
   */
  private final SortedMap<String, Long> last_views = new TreeMap();
  /**
   * Log of pre-prepare/prepare messages corresponding to requests that
   * prepared or pre-prepared in previous views
   */
  private final SeqNumLog<OReqInfo> oplog;
  /**
   * View-change acks sent in the current view by this replica
   */
  private final List<ViewChangeAcknowledgementMessage> my_vacks =
      new ArrayList<>();
  /**
   * Buffered acknowledgements from other replica
   */
  private final List<VCAInfo> vacks = new ArrayList<>();
  /**
   * New-view messages with the highest view number "vn" for each replica (such
   * that "vn >= v") and associated view-change messages. It is indexed by
   * replica ID.
   */
  private final SortedMap<String, NVInfo> last_nvs = new TreeMap<>();
  /**
   * Time at which my view-change was last sent
   */
  private Instant vc_sent;
  /**
   * Last sequence number known to be stable
   */
  private long last_stable;

  /**
   * Constructor for the ViewInfo class
   *
   * @param replica replica
   * @param v       view number
   */
  public ViewInfo(PbftReplica replica, long v) {
    this.replica = replica;
    this.v = v;
    this.oplog =
        new SeqNumLog<>(replica.getConfig().getMAX_OUT(), OReqInfo::new);
  }

  /**
   * Adds "pp" to "this". This becomes the owner of "pp"'s storage.
   * Requires "pp->view() == view()" and "pp" was part of a complete prepared
   * certificate
   *
   * @param pp the PrePrepareMessage to add
   */
  public void addComplete(PrePrepareMessage pp) {
    throw new UnsupportedOperationException("Not implemented");
  }

  /**
   * Adds information to this. Requires the replica sent a pre-prepare/prepare
   * for sequence number "n" in view() and the request did not prepare locally.
   *
   * @param n sequence number
   * @param d digest
   */
  public void addIncomplete(long n, Digest d) {
    throw new UnsupportedOperationException("Not implemented");
  }

  /**
   * If there is a pre-repare for sequence number "n" with view greater
   * than or equal to "v", return it. Otherwise, return null.
   *
   * @param n sequence number
   * @param d digest
   * @return the PrePrepareMessage if it exists
   */
  public Optional<PrePrepareMessage> prePrepare(long n, Digest d) {
    throw new UnsupportedOperationException("Not implemented");
  }

  /**
   * If there is a pre-repare logged for sequence number "n" with view greater
   * than or equal to "v", return it. Otherwise, return null.
   *
   * @param n    sequence number
   * @param view view number
   * @return the PrePrepareMessage if it exists
   */
  public Optional<PrePrepareMessage> prePrepare(long n, long view) {
    throw new UnsupportedOperationException("Not implemented");
  }

  /**
   * Returns true iff "this" logs that this replica sent a prepare with digest
   * "d" for sequence number "n"
   *
   * @param n sequence number
   * @param d digest
   * @return true if the prepare was sent
   */
  public boolean prepare(long n, Digest d) {
    throw new UnsupportedOperationException("Not implemented");
  }

  /**
   * Moves this to view "v", discards messages for views less than v,
   * sends a new authenticated view-change message for view "v", and
   * sends view-change acks for any logged view-change messages from
   * other replicas with view "v".
   * Requires all PrePrepare messages in complete certificates for views
   * less than "view()" have been added to "this".
   *
   * @param v     view number
   * @param le    last sequence number known to be stable
   * @param state state
   */
  public void viewChange(long v, long le, State state) {
    throw new UnsupportedOperationException("Not implemented");
  }

  /**
   * Stores message if it is valid and if it is not obslete, otherwise discards
   * it. This becomes the owner of "vc"'s storage.
   *
   * @param vc the ViewChangeMessage to add
   * @return true iff "vc" is not discarded
   */
  public boolean add(ViewChangeMessage vc) {
    throw new UnsupportedOperationException("Not implemented");
  }

  /**
   * Stores message if it is valid and if it is not obslete, otherwise discards
   * it. This becomes the owner of "nv"'s storage.
   *
   * @param nv the NewViewMessage to add
   */
  public void add(NewViewMessage nv) {
    throw new UnsupportedOperationException("Not implemented");
  }

  /**
   * Adds message to this if valid and useful
   *
   * @param vca the ViewChangeAcknowledgementMessage to add
   */
  public void add(ViewChangeAcknowledgementMessage vca) {
    throw new UnsupportedOperationException("Not implemented");
  }

  /**
   * Returns the maximum view number "v" for which it is known that some correct
   * replica is in view "v" or a later view.
   *
   * @return the maximum view number
   */
  public long getMaxView() {
    throw new UnsupportedOperationException("Not implemented");
  }

  /**
   * Return the maximum view number "v" for which it is known that
   * f+1 correct replicas are in view "v" or a later view.
   *
   * @return the maximum view number
   */
  public long maxMajView() {
    throw new UnsupportedOperationException("Not implemented");
  }

  /**
   * Return the current view
   *
   * @return the current view
   */
  public long view() {
    throw new UnsupportedOperationException("Not implemented");
  }

  /**
   * Returns true iff this contains complete new-view information for view v.
   *
   * @param v view number
   * @return true iff contains complete new-view information for view v
   */
  public boolean hasNewView(long v) {
    throw new UnsupportedOperationException("Not implemented");
  }

  /**
   * Check if this contains a new-view message for view v
   *
   * @param v view number
   * @return true iff the new-view message exists
   */
  public boolean hasNvMessage(long v) {
    throw new UnsupportedOperationException("Not implemented");
  }

  /**
   * Mutates "m" to record which view change messages were accepted in the
   * current view. Requires m.view() == view().
   *
   * @param m the StatusMessage to mutate
   */
  public void setReceivedVcs(StatusMessage m) {
    throw new UnsupportedOperationException("Not implemented");
  }

  /**
   * Mutates "m" to record which pre-prepare messages are
   * missing in the current view and whether a proof of authenticity
   * for the associated request is required.
   * Requires m.view() == view().
   *
   * @param m the StatusMessage to mutate
   */
  public void setMissingPps(StatusMessage m) {
    throw new UnsupportedOperationException("Not implemented");
  }

  /**
   * Returns the view-change message sent by the calling replica in view
   * "view()" and the time at which it was sent.
   *
   * @return the view-change message
   */
  public MessageWithTime<ViewChangeMessage> my_view_change() {
    throw new UnsupportedOperationException("Not implemented");
  }

  /**
   * Returns any new-view message sent by the calling replica in view "view()",
   * or null if none exists along with the time at which it was sent.
   *
   * @return the new-view message
   */
  public MessageWithTime<NewViewMessage> myNewView() {
    throw new UnsupportedOperationException("Not implemented");
  }

  /**
   * Returns any view-change ack produced by the calling replica or 0 if there
   * is no such message.
   *
   * @param id the replica ID
   * @return the view-change ack
   */
  public Optional<ViewChangeAcknowledgementMessage> my_vc_ack(String id) {
    throw new UnsupportedOperationException("Not implemented");
  }

  /**
   * Marks all requests with sequence number greater than or equal to "ls" as
   * stable.
   *
   * @param n the sequence number
   */
  public void markStable(long n) {
    throw new UnsupportedOperationException("Not implemented");
  }

  /**
   * Marks all the view-change and new-view messages in this as stale. Except
   * view-change messages sent by this or complete new-view messages.
   */
  public void markStale() {
    for (String key : last_vcs.keySet()) {
      if (!this.replica.id().equals(key)) {
        last_vcs.remove(key);
        if (last_views.containsKey(key) && last_views.get(key) >= v) {
          last_views.put(key, v);
        }
      }

      ViewChangeMessage vc = last_nvs.get(key).mark_stale(this.replica.id());
      if (vc != null && vc.getViewNumber() == view()) {
        last_vcs.put(this.replica.id(), vc);
      }
    }

    // FIXME: might not be a full clear?
    my_vacks.clear();
    vacks.clear();
  }

  /**
   * Removes all messages from this except that it retains log for prepares in
   * old views
   */
  public void clear() {
    throw new UnsupportedOperationException("Not implemented");
  }

  /**
   * Sets "d" to the digest of the request with sequence number "n". If enough
   * information to make a pre-prepare is available, it returns an appropriate
   * pre-prepare. Otherwise, returns null.
   * Requires "has_new_view(view())" and "n" is a request in new-view message.
   *
   * @param n sequence number
   * @param d digest
   * @return the PrePrepareMessage if it exists
   */
  public Optional<PrePrepareMessage> fetchRequest(long n, byte[] d) {
    throw new UnsupportedOperationException("Not implemented");
  }

  /**
   * Checks if pp is a pre-prepare that the replica needs to complete the
   * new-view information. If it is stores pp, otherwise deletes pp.
   *
   * @param pp the PrePrepareMessage to check
   */
  public void addMissing(PrePrepareMessage pp) {
    throw new UnsupportedOperationException("Not implemented");
  }

  /**
   * Records that the big request with digest "rd" that is
   * referenced by a pre-prepare with sequence number "n" as the i-th
   * big request is cached.
   *
   * @param rd the digest
   * @param n  the sequence number
   * @param i  the index
   */
  public void addMissing(byte[] rd, long n, int i) {
    throw new UnsupportedOperationException("Not implemented");
  }

  /**
   * Checks if p is a prepare that the replica needs to complete the new-view
   * information. If it is, stores p, otherwise deletes p.
   *
   * @param p the PrepareMessage to check
   */
  public void addMissing(PrepareMessage p) {
    throw new UnsupportedOperationException("Not implemented");
  }

  /**
   * Sends prepare messages for any logged digests for sequence number "n"
   * sent in a view greater than or equal to "v" to dest
   *
   * @param n    sequence number
   * @param v    view number
   * @param dest destination replica ID
   */
  public void sendProofs(long n, long v, String dest) {
    throw new UnsupportedOperationException("Not implemented");
  }

  /**
   * Shuts down this writing value to "o"
   *
   * @return true if the shutdown was successful
   */
  public boolean shutdown() {
    throw new UnsupportedOperationException("Not implemented");
  }

  /**
   * Restarts this from value in "i"
   *
   * @param i  the value to restart from
   * @param rv the view number
   * @param ls the last sequence number known to be stable
   * @return true if the restart was successful
   */
  public boolean restart(String i, long rv, long ls, boolean corrupt) {
    throw new UnsupportedOperationException("Not implemented");
  }

  /**
   * Enforces that there is no information about bound "b".
   * "ks" is the maximum sequence number that I know is stable.
   *
   * @param b       bound
   * @param ks      maximum sequence number known to be stable
   * @param corrupt corrupt
   * @return true if the bound was enforced
   */
  public boolean enforceBound(long b, long ks, boolean corrupt) {
    throw new UnsupportedOperationException("Not implemented");
  }

  /**
   * ODigestInfo holds the view and digest of a request that was prepared
   */
  @Data
  private class ODigestInfo {
    private final long v = -1;
    private Digest d;
  }

  /**
   * OReqInfo describes requests that (1) prepared or (2) for which a
   * pre-prepare/prepare message was sent.
   * <p>
   * In case (1):
   * - "m" points to the pre-prepare message in the last complete
   * prepared certificate for the corresponding sequence number.
   * - "v" and "d" are the view and digest in this message; and
   * - "lv" is the latest view for which this replica sent a
   * pre-prepare/prepare matching "m".
   * <p>
   * In case (2):
   * - "m" is null;
   * - "v" and "d" are the view and digest in the last
   * pre-prepare/prepare sent by this replica for the corresponding
   * sequence number.
   * - no request prepared globally with sequence number "n" in any view
   * "v' <= lv".
   */
  @Data
  private class OReqInfo implements SeqNumLog.SeqNumLogEntry {
    private long lv;
    private long v;
    private Digest d;
    private List<PrePrepareMessage> m = new ArrayList<>();
    private List<ODigestInfo> ods = new ArrayList<>();

    public void clear() {
      throw new UnsupportedOperationException("Not implemented");
    }
  }

  /**
   * Acknowledgements from other replicas
   */
  @Data
  private class VCAInfo {
    private long v;
    private List<ViewChangeAcknowledgementMessage> vacks = new ArrayList<>();

    public VCAInfo() {
      throw new UnsupportedOperationException("Not implemented");
    }

    void clear() { throw new UnsupportedOperationException("Not implemented"); }
  }
}
