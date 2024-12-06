package byzzbench.simulator.protocols.fab;

import byzzbench.simulator.protocols.fab.messages.AcceptMessage;
import byzzbench.simulator.protocols.fab.messages.ProposeMessage;
import byzzbench.simulator.protocols.fab.replica.FabReplicaTicketPhase;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;
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
    private final transient AtomicReference<FabReplicaTicketPhase> phase = new AtomicReference<>(FabReplicaTicketPhase.PROPOSE);
    @Getter
    private final transient CompletableFuture<R> result = new CompletableFuture<>();
    @Getter
    private volatile ProposeMessage request;

    public void append(Serializable message) {
        this.messages.add(message);

        if (this.request == null) {
            if (message instanceof ProposeMessage proposeMessage) {
                this.request = proposeMessage;
            }
        }
    }

    public boolean isAccepted(int threshold) {
        int acceptCount = 0;

        for (Serializable message : this.messages) {
            if (message instanceof AcceptMessage acceptMessage) {
                if (acceptMessage.getAcceptCount() >= threshold) {
                    return true;
                }
            }
        }

        return acceptCount >= threshold;
    }





}
