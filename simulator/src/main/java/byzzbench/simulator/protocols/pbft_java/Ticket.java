package byzzbench.simulator.protocols.pbft_java;

import byzzbench.simulator.protocols.pbft_java.message.CommitMessage;
import byzzbench.simulator.protocols.pbft_java.message.PrePrepareMessage;
import byzzbench.simulator.protocols.pbft_java.message.PrepareMessage;
import byzzbench.simulator.protocols.pbft_java.message.RequestMessage;
import byzzbench.simulator.utils.DeterministicCompletableFuture;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;

@RequiredArgsConstructor
public class Ticket<O extends Serializable, R extends Serializable> implements Serializable {
    @Getter
    private final long viewNumber;

    @Getter
    private final long seqNumber;

    @Getter
    private final Collection<Serializable> messages = new ConcurrentLinkedQueue<>();
    private final transient AtomicReference<ReplicaTicketPhase> phase = new AtomicReference<>(ReplicaTicketPhase.PRE_PREPARE);
    @Getter
    private final transient CompletableFuture<R> result = new DeterministicCompletableFuture<>();
    @Getter
    private volatile RequestMessage request;

    public void append(Serializable message) {
        this.messages.add(message);

        if (this.request == null) {
            if (message instanceof RequestMessage requestMessage) {
                this.request = requestMessage;
            } else if (message instanceof PrePrepareMessage prePrepareMessage) {
                this.request = prePrepareMessage.getRequest();
            }
        }
    }

    private boolean matchesPrePrepare(PrePrepareMessage prePrepare, PrepareMessage prepare) {
        return Arrays.equals(prePrepare.getDigest(), prepare.getDigest());
    }

    public boolean isPrepared(int tolerance) {
        final int requiredMatches = 2 * tolerance;

        for (Object prePrepareObject : this.messages) {
            if (!(prePrepareObject instanceof PrePrepareMessage prePrepare)) {
                continue;
            }

            int matchingPrepares = 0;
            for (Object prepareObject : this.messages) {
                if (!(prepareObject instanceof PrepareMessage prepare)) {
                    continue;
                }

                if (!this.matchesPrePrepare(prePrepare, prepare)) {
                    continue;
                }

                matchingPrepares++;
                if (matchingPrepares == requiredMatches) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean isCommittedLocal(int tolerance) {
        // this is checked after the PREPARE phase is
        // chcked in DefaultReplica so it is safe to
        // assume that {@code prepared} is true
        final int requiredCommits = 2 * tolerance + 1;
        int commits = 0;
        for (Object message : this.messages) {
            if (message instanceof CommitMessage) {
                commits++;
            }
        }

        return commits >= requiredCommits;
    }

    public boolean casPhase(ReplicaTicketPhase old, ReplicaTicketPhase next) {
        return this.phase.compareAndSet(old, next);
    }

    public ReplicaTicketPhase getPhase() {
        return this.phase.get();
    }
}
