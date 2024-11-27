package byzzbench.simulator.protocols.hbft.mutator;

import byzzbench.simulator.faults.FaultContext;
import byzzbench.simulator.faults.faults.MessageMutationFault;
import byzzbench.simulator.faults.factories.MessageMutatorFactory;
import byzzbench.simulator.protocols.hbft.message.CheckpointIIMessage;
import byzzbench.simulator.transport.Event;
import byzzbench.simulator.transport.MessageEvent;
import lombok.ToString;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@ToString
public class CheckpointIIMessageMutatorFactory extends MessageMutatorFactory {
    RuntimeException invalidMessageTypeException = new IllegalArgumentException("Invalid message type");

    @Override
    public List<MessageMutationFault> mutators() {
        return List.of(
                new MessageMutationFault("hbft-checkpointII-different-digest", "Change digest", List.of(CheckpointIIMessage.class)) {
                    @Override
                    public void accept(FaultContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof CheckpointIIMessage message)) {
                            throw invalidMessageTypeException;
                        }
                        // Create a random digest
                        byte[] digest = new byte[20];
                        CheckpointIIMessage mutatedMessage = message.withDigest(digest);
                        mutatedMessage.sign(message.getSignedBy());
                        messageEvent.setPayload(mutatedMessage);
                    }
                },
                new MessageMutationFault("hbft-checkpointII-seq-inc", "Increment Sequence Number", List.of(CheckpointIIMessage.class)) {
                    @Override
                    public void accept(FaultContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof CheckpointIIMessage message)) {
                            throw invalidMessageTypeException;
                        }

                        CheckpointIIMessage mutatedMessage = message.withLastSeqNumber(message.getLastSeqNumber() + 1);
                        mutatedMessage.sign(message.getSignedBy());
                        messageEvent.setPayload(mutatedMessage);
                    }
                },
                new MessageMutationFault("hbft-checkpointII-seq-dec", "Decrement Sequence Number", List.of(CheckpointIIMessage.class)) {
                    @Override
                    public void accept(FaultContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof CheckpointIIMessage message)) {
                            throw invalidMessageTypeException;
                        }
                        CheckpointIIMessage mutatedMessage = message.withLastSeqNumber(message.getLastSeqNumber() - 1);
                        mutatedMessage.sign(message.getSignedBy());
                        messageEvent.setPayload(mutatedMessage);
                    }
                }
        );
    }
}
