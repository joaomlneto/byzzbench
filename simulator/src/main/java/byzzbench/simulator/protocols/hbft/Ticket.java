package byzzbench.simulator.protocols.hbft;

import byzzbench.simulator.protocols.hbft.message.CommitMessage;
import byzzbench.simulator.protocols.hbft.message.PrepareMessage;
import byzzbench.simulator.protocols.hbft.message.RequestMessage;
import byzzbench.simulator.protocols.hbft.pojo.ReplicaTicketPhase;
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
    private final transient AtomicReference<ReplicaTicketPhase> phase = new AtomicReference<>(ReplicaTicketPhase.PREPARE);
    @Getter
    private final transient CompletableFuture<R> result = new CompletableFuture<>();
    @Getter
    private volatile RequestMessage request;

    public void append(Serializable message) {
        this.messages.add(message);

        if (this.request == null) {
            if (message instanceof RequestMessage requestMessage) {
                this.request = requestMessage;
            } else if (message instanceof PrepareMessage prepareMessage) {
                this.request = prepareMessage.getRequest();
            }
        }
    }

    public boolean isPrepared(int tolerance) {
        final int requiredMatches = 2 * tolerance;
        int matchingPrepares = 0;

        for (Object prepareObject : this.messages) {
            if (!(prepareObject instanceof PrepareMessage)) {
                continue;
            }

            matchingPrepares++;
            if (matchingPrepares == requiredMatches) {
                return true;
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
