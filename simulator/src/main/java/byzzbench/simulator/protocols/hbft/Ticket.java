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

import byzzbench.simulator.protocols.hbft.message.ReplyMessage;

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
    @Getter
    private volatile PrepareMessage prepare;
    @Getter
    private volatile ReplyMessage reply;

    public void append(Serializable message) {
        this.messages.add(message);

        //if (this.request == null) {
            if (message instanceof RequestMessage requestMessage) {
                this.request = requestMessage;
            } else if (message instanceof PrepareMessage prepareMessage) {
                this.request = prepareMessage.getRequest();
                this.prepare = prepareMessage;
            } else if (message instanceof ReplyMessage replyMessage) {
                this.reply = replyMessage;
            }
        //}
    }

    public boolean isCommittedLocal(int tolerance) {
        final int requiredCommits = 2 * tolerance + 1;
        int commits = 0;
        for (Object message : this.messages) {
            if (message instanceof CommitMessage) {
                commits++;
            }
        }

        return commits >= requiredCommits;
    }

    public boolean isCommittedConflicting(int tolerance) {
        final int requiredCommits = tolerance + 1;
        int commits = 0;
        for (Object message : this.messages) {
            if (message instanceof CommitMessage commitMessage) {
                //System.out.println(this.viewNumber + " " + this.seqNumber);
                if (!commitMessage.getRequest().equals(this.request) || !Arrays.equals(commitMessage.getDigest(), this.prepare.getDigest())) {
                    commits++;
                }
            }
        }

        return commits >= requiredCommits;
    }

    public boolean isPrepared(int tolerance) {
        final int requiredCommits = tolerance + 1;
        int commits = 0;
        for (Object message : this.messages) {
            if (message instanceof CommitMessage commitMessage) {
                commits++;

                if (commits >= requiredCommits) {
                    this.request = commitMessage.getRequest();
                    return true;
                }
            }
        }

        return false;
    }

    public boolean casPhase(ReplicaTicketPhase old, ReplicaTicketPhase next) {
        return this.phase.compareAndSet(old, next);
    }

    public ReplicaTicketPhase getPhase() {
        return this.phase.get();
    }
}
