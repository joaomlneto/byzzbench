package byzzbench.simulator.protocols.pbft_java;

import byzzbench.simulator.protocols.pbft_java.message.*;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import lombok.Getter;
import lombok.NonNull;

public class MessageLog implements Serializable {
  private static final byte[] NULL_DIGEST = new byte[0];
  private static final RequestMessage NULL_REQ =
      new RequestMessage(null, 0, "");

  private final int bufferThreshold;
  @Getter private final int checkpointInterval;
  @Getter private final int watermarkInterval;

  @Getter
  private final Deque<RequestMessage> buffer = new ConcurrentLinkedDeque<>();

  @Getter
  private final SortedMap<ReplicaRequestKey, Ticket<?, ?>> ticketCache =
      new TreeMap<>();
  @Getter
  private final SortedMap<TicketKey, Ticket<?, ?>> tickets = new TreeMap<>();

  @Getter
  private final SortedMap<Long, Collection<CheckpointMessage>> checkpoints =
      new TreeMap<>();
  @Getter
  private final SortedMap<Long, SortedMap<String, ViewChangeMessage>>
      viewChanges = new TreeMap<>();

  private volatile long lowWaterMark;
  private volatile long highWaterMark;

  public MessageLog(int bufferThreshold, int checkpointInterval,
                    int watermarkInterval) {
    this.bufferThreshold = bufferThreshold;
    this.checkpointInterval = checkpointInterval;
    this.watermarkInterval = watermarkInterval;

    this.lowWaterMark = 0;
    this.highWaterMark = this.lowWaterMark + watermarkInterval;
  }

  public <O extends Serializable, R extends Serializable> Ticket<O, R>
  getTicketFromCache(ReplicaRequestKey key) {
    // return (Ticket<O, R>) this.ticketCache.get(key);

    if (this.ticketCache.containsKey(key)) {
      return (Ticket<O, R>)this.ticketCache.get(key);
    } else {
      return null;
    }
  }

  public <O extends Serializable, R extends Serializable> Ticket<O, R>
  getTicket(long viewNumber, long seqNumber) {
    TicketKey key = new TicketKey(viewNumber, seqNumber);
    return (Ticket<O, R>)this.tickets.get(key);
  }

  public @NonNull<O extends Serializable, R extends Serializable> Ticket<O, R>
  newTicket(long viewNumber, long seqNumber) {
    TicketKey key = new TicketKey(viewNumber, seqNumber);
    return (Ticket<O, R>)this.tickets.computeIfAbsent(
        key, k -> new Ticket<>(viewNumber, seqNumber));
  }

  public boolean completeTicket(ReplicaRequestKey rrk, long viewNumber,
                                long seqNumber) {
    TicketKey key = new TicketKey(viewNumber, seqNumber);
    Ticket<?, ?> ticket = this.tickets.remove(key);

    this.ticketCache.put(rrk, ticket);

    return ticket != null;
  }

  private void gcCheckpoint(long checkpoint) {
    /*
     * Procedure used to discard all PRE-PREPARE, PREPARE and COMMIT
     * messages with sequence number less than or equal the in addition to
     * any prior checkpoint proof per PBFT 4.3.
     *
     * A stable checkpoint then allows the water marks to slide over to
     * the checkpoint < x <= checkpoint + watermarkInterval per PBFT 4.3.
     */
    for (Map.Entry<ReplicaRequestKey, Ticket<?, ?>> entry :
         this.ticketCache.entrySet()) {
      Ticket<?, ?> ticket = entry.getValue();
      if (ticket.getSeqNumber() <= checkpoint) {
        this.ticketCache.remove(entry.getKey());
      }
    }

    for (Long seqNumber : this.checkpoints.keySet()) {
      if (seqNumber < checkpoint) {
        this.checkpoints.remove(seqNumber);
      }
    }

    this.highWaterMark = checkpoint + this.watermarkInterval;
    this.lowWaterMark = checkpoint;
  }

  public void appendCheckpoint(CheckpointMessage checkpoint, int tolerance) {
    /*
     * Per PBFT 4.3, each time a checkpoint is generated or received, it
     * gets stored in the log until 2f + 1 are accumulated that have
     * matching digests to the checkpoint that was added to the log, in
     * which case the garbage collection occurs (see #gcCheckpoint(long)).
     */
    long seqNumber = checkpoint.getLastSeqNumber();
    Collection<CheckpointMessage> checkpointProofs =
        this.checkpoints.computeIfAbsent(seqNumber,
                                         k -> new ConcurrentLinkedQueue<>());
    checkpointProofs.add(checkpoint);

    final int stableCount = 2 * tolerance + 1;
    int matching = 0;

    // Use a loop here to avoid the linked list being traversed in its
    // entirety
    for (CheckpointMessage proof : checkpointProofs) {
      if (Arrays.equals(proof.getDigest(), checkpoint.getDigest())) {
        matching++;

        if (matching == stableCount) {
          this.gcCheckpoint(seqNumber);
          return;
        }
      }
    }
  }

  private Collection<IPhaseMessage> selectPreparedProofs(Ticket<?, ?> ticket,
                                                         int requiredMatches) {
    /*
     * Selecting the proofs of PRE-PREPARE and PREPARE messages for the
     * VIEW-CHANGE vote per PBFT 4.4.
     *
     * This procedure is designed to be run over each ReplicaTicket and
     * collects the PRE-PREPARE for the ticket and the required PREPARE
     * messages, otherwise returning null if there were not enough
     * PREPARE messages or PRE-PREPARE has not been received yet.
     */
    Collection<IPhaseMessage> proof = new ArrayList<>();
    for (Object prePrepareObject : ticket.getMessages()) {
      if (!(prePrepareObject instanceof PrePrepareMessage prePrepare)) {
        continue;
      }

      proof.add(prePrepare);

      int matchingPrepares = 0;
      for (Object prepareObject : ticket.getMessages()) {
        if (!(prepareObject instanceof PrepareMessage prepare)) {
          continue;
        }

        if (!Arrays.equals(prePrepare.getDigest(), prepare.getDigest())) {
          continue;
        }

        matchingPrepares++;
        proof.add(prepare);

        if (matchingPrepares == requiredMatches) {
          return proof;
        }
      }
    }

    return null;
  }

  public ViewChangeMessage produceViewChange(long newViewNumber,
                                             String replicaId, int tolerance) {
    /*
     * Produces a VIEW-CHANGE vote message in accordance with PBFT 4.4.
     *
     * The last stable checkpoint is defined as the low water mark for the
     * message log. The checkpoint proofs are provided each time the
     * checkpoint advances, or could possibly be empty if the checkpoint
     * is still at 0 (i.e. starting state).
     *
     * Proofs are gathered through #selectPreparedProofs(...) with 2f
     * required PREPARE messages.
     */
    long checkpoint = this.lowWaterMark;

    Collection<CheckpointMessage> checkpointProofs =
        checkpoint == 0 ? Collections.emptyList()
                        : this.checkpoints.get(checkpoint);
    if (checkpointProofs == null) {
      throw new IllegalStateException(
          "Checkpoint has diverged without any proof");
    }

    final int requiredMatches = 2 * tolerance;
    SortedMap<Long, Collection<IPhaseMessage>> preparedProofs = new TreeMap<>();

    // Scan through the ticket cache (i.e. the completed tickets)
    for (Ticket<?, ?> ticket : this.ticketCache.values()) {
      long seqNumber = ticket.getSeqNumber();
      if (seqNumber > checkpoint) {
        Collection<IPhaseMessage> proofs =
            this.selectPreparedProofs(ticket, requiredMatches);
        if (proofs == null) {
          continue;
        }

        preparedProofs.put(seqNumber, proofs);
      }
    }

    // Scan through the currently active tickets
    for (Ticket<?, ?> ticket : this.tickets.values()) {
      ReplicaTicketPhase phase = ticket.getPhase();
      if (phase == ReplicaTicketPhase.PRE_PREPARE) {
        continue;
      }

      long seqNumber = ticket.getSeqNumber();
      if (seqNumber > checkpoint) {
        Collection<IPhaseMessage> proofs =
            this.selectPreparedProofs(ticket, requiredMatches);
        if (proofs == null) {
          continue;
        }

        preparedProofs.put(seqNumber, proofs);
      }
    }

    ViewChangeMessage viewChange = new ViewChangeMessage(
        newViewNumber, checkpoint, checkpointProofs, preparedProofs, replicaId);

    /*
     * Potentially non-standard behavior - PBFT 4.5.2 does not specify
     * whether replicas include their own view change messages. For 3f + 1
     * replicas in the system, then given the max f faulty nodes, 3f + 1 - f
     * or 2f + 1 replicas are expected to vote, meaning that excluding the
     * initiating replica reduces the total number of votes to 2f. Since
     * PBFT 4.5.2 states that the next view change may only be initiated by
     * a quorum of 2f + 1 replicas, then electing a faulty primary that does
     * not multicast a NEW-VIEW message will cause the entire system to
     * stall; therefore I do include the initiating replica here.
     */
    SortedMap<String, ViewChangeMessage> newViewSet =
        this.viewChanges.computeIfAbsent(newViewNumber, k -> new TreeMap<>());
    newViewSet.put(replicaId, viewChange);

    return viewChange;
  }

  public ViewChangeResult acceptViewChange(ViewChangeMessage viewChange,
                                           String curReplicaId,
                                           long curViewNumber, int tolerance) {
    /*
     * Per PBFT 4.4, a received VIEW-CHANGE vote is stored into the message
     * log and the state is returned to the replica as
     * ReplicaViewChangeResult.
     *
     * The procedure first computes the total number of votes from other
     * replicas that try to move the view a higher view number. If this
     * number of other relicas is equal to the bandwagon size, then this
     * replica contributes its vote once to avoid creating an infinite
     * response loop and taking up the network capacity.
     *
     * Secondly, this procedure finds the smallest view the system is
     * attempting to elect and selects that to bandwagon.
     *
     * Finally, this procedure determines if the number of votes is enough
     * to restart the timer to move to the view after the one now being
     * elected in the case that the candidate view has a faulty primary.
     */
    long newViewNumber = viewChange.getNewViewNumber();
    String replicaId = viewChange.getReplicaId();

    SortedMap<String, ViewChangeMessage> newViewSet =
        this.viewChanges.computeIfAbsent(newViewNumber, k -> new TreeMap<>());
    newViewSet.put(replicaId, viewChange);

    final int bandwagonSize = tolerance + 1;

    int totalVotes = 0;
    long smallestView = Long.MAX_VALUE;
    for (SortedMap.Entry<Long, SortedMap<String, ViewChangeMessage>> entry :
         this.viewChanges.entrySet()) {
      long entryView = entry.getKey();
      if (entryView <= curViewNumber) {
        continue;
      }

      SortedMap<String, ViewChangeMessage> votes = entry.getValue();
      int entryVotes = votes.size();

      /*
       * See #produceViewChange(...)
       * Subtract the current replica's vote to obtain the votes from the
       * other replicas
       */
      if (votes.containsKey(curReplicaId)) {
        entryVotes--;
      }

      totalVotes += entryVotes;

      if (smallestView > entryView) {
        smallestView = entryView;
      }
    }

    boolean shouldBandwagon = totalVotes == bandwagonSize;

    final int timerThreshold = 2 * tolerance + 1;
    boolean beginNextVote = newViewSet.size() >= timerThreshold;

    return new ViewChangeResult(shouldBandwagon, smallestView, beginNextVote);
  }

  private Collection<PrePrepareMessage>
  selectPreparedProofs(long newViewNumber, long minS, long maxS,
                       SortedMap<Long, PrePrepareMessage> prePrepareMap) {
    /*
     * This procedure computes the prepared proofs for the NEW-VIEW message
     * that is sent by the primary when it is elected in accordance with
     * PBFT 4.4. It adds messages in between the min-s and max-s sequences,
     * including any missing messages by using a no-op PRE-PREPARE message.
     *
     * Non-standard behavior - PBFT 4.4 specifies that PRE-PREPARE messages
     * are to be sent without their requests, but again, this is up to the
     * transport to decide how to work. For simplicity, the default
     * implementation sends the request along with the PRE-PREPARE as
     * explained in DefaultReplica.
     */
    Collection<PrePrepareMessage> sequenceProofs = new ArrayList<>();
    for (long i = minS; minS != maxS && i <= maxS; i++) {
      PrePrepareMessage prePrepareProofMessage = prePrepareMap.get(i);
      if (prePrepareProofMessage == null) {
        prePrepareProofMessage =
            new PrePrepareMessage(newViewNumber, i, NULL_DIGEST, NULL_REQ);
      }

      sequenceProofs.add(prePrepareProofMessage);

      Ticket<Serializable, Serializable> ticket =
          this.newTicket(newViewNumber, i);
      ticket.append(prePrepareProofMessage);
    }

    return sequenceProofs;
  }

  public NewViewMessage produceNewView(long newViewNumber, String replicaId,
                                       int tolerance) {
    /*
     * Produces the NEW-VIEW message to notify the other replicas of the
     * elected primary in accordance with PBFT 4.4.
     *
     * If there is not a quorum of votes for this replica to become the
     * primary excluding this replica's own vote, then do not proceed.
     *
     * This scans through all VIEW-CHANGE votes for their checkpoint proofs
     * and their prepared proofs to look for the min and max sequence
     * numbers to generate the final PREPARE proofs
     * (see #selectPrepareProofs(...)). These values are also used to
     * update the water marks and passed through the proof map.
     *
     * The VIEW-CHANGE votes are then added in addition to the PREPARE
     * proofs to the NEW-VIEW message.
     */

    SortedMap<String, ViewChangeMessage> newViewSet =
        this.viewChanges.get(newViewNumber);
    int votes = newViewSet.size();
    boolean hasOwnViewChange = newViewSet.containsKey(replicaId);
    if (hasOwnViewChange) {
      votes--;
    }

    final int quorum = 2 * tolerance;
    if (votes < quorum) {
      return null;
    }

    long minS = Long.MAX_VALUE;
    long maxS = Long.MIN_VALUE;
    Collection<CheckpointMessage> minSProof = null;
    SortedMap<Long, PrePrepareMessage> prePrepareMap = new TreeMap<>();
    for (ViewChangeMessage viewChange : newViewSet.values()) {
      long seqNumber = viewChange.getLastSeqNumber();
      Collection<CheckpointMessage> proofs = viewChange.getCheckpointProofs();
      if (seqNumber < minS) {
        minS = seqNumber;
        minSProof = proofs;
      }

      if (seqNumber > maxS) {
        maxS = seqNumber;
      }

      for (Map.Entry<Long, Collection<IPhaseMessage>> entry :
           viewChange.getPreparedProofs().entrySet()) {
        long prePrepareSeqNumber = entry.getKey();
        if (prePrepareSeqNumber > maxS) {
          maxS = prePrepareSeqNumber;
        }

        for (IPhaseMessage phaseMessage : entry.getValue()) {
          if (!(phaseMessage instanceof PrePrepareMessage)) {
            continue;
          }

          prePrepareMap.put(prePrepareSeqNumber,
                            (PrePrepareMessage)phaseMessage);
          break;
        }
      }
    }

    this.gcNewView(newViewNumber);
    if (minS > this.lowWaterMark) {
      this.checkpoints.put(minS, minSProof);
      this.gcCheckpoint(minS);
    }

    Collection<ViewChangeMessage> viewChangeProofs =
        new ArrayList<>(newViewSet.values());
    viewChangeProofs.addAll(newViewSet.values());
    if (!hasOwnViewChange) {
      viewChangeProofs.add(
          this.produceViewChange(newViewNumber, replicaId, tolerance));
    }

    Collection<PrePrepareMessage> preparedProofs =
        this.selectPreparedProofs(newViewNumber, minS, maxS, prePrepareMap);

    return new NewViewMessage(newViewNumber, viewChangeProofs, preparedProofs);
  }

  private void gcNewView(long newViewNumber) {
    /*
     * Performs clean-up for entering a new view in accordance with PBFT
     * 4.4. This means that any view change votes and pending tickets that
     * are not in the new view are removed.
     */
    this.viewChanges.remove(newViewNumber);

    for (TicketKey key : this.tickets.keySet()) {
      if (key.getViewNumber() != newViewNumber) {
        this.tickets.remove(key);
      }
    }
  }

  public boolean acceptNewView(NewViewMessage newView) {
    /*
     * Verify the change to a new view in accordance with PBFT 4.4 and then
     * find the min-s value and update the low water mark if it is lagging
     * behind the new view.
     */
    long newViewNumber = newView.getNewViewNumber();
    this.gcNewView(newViewNumber);

    long minS = Integer.MAX_VALUE;
    Collection<CheckpointMessage> checkpointProofs = null;
    Collection<ViewChangeMessage> viewChangeProofs =
        newView.getViewChangeProofs();
    for (ViewChangeMessage viewChange : viewChangeProofs) {
      if (newViewNumber != viewChange.getNewViewNumber()) {
        return false;
      }

      long seqNumber = viewChange.getLastSeqNumber();
      if (seqNumber < minS) {
        minS = seqNumber;
        checkpointProofs = viewChange.getCheckpointProofs();
      }
    }

    if (this.lowWaterMark < minS) {
      this.checkpoints.put(minS, checkpointProofs);
      this.gcCheckpoint(minS);
    }

    return true;
  }

  public boolean shouldBuffer() {
    return this.tickets.size() >= this.bufferThreshold;
  }

  public <O> void buffer(RequestMessage request) {
    this.buffer.addLast(request);
  }

  public <O> RequestMessage popBuffer() { return this.buffer.pollFirst(); }

  public boolean isBetweenWaterMarks(long seqNumber) {
    return seqNumber >= this.lowWaterMark && seqNumber <= this.highWaterMark;
  }
}
