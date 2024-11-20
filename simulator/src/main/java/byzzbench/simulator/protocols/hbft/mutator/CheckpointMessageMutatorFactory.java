package byzzbench.simulator.protocols.hbft.mutator;

import byzzbench.simulator.faults.FaultContext;
import byzzbench.simulator.faults.MessageMutationFault;
import byzzbench.simulator.faults.MessageMutatorFactory;
import byzzbench.simulator.protocols.hbft.message.CheckpointMessage;
import byzzbench.simulator.transport.Event;
import byzzbench.simulator.transport.MessageEvent;
import lombok.ToString;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import byzzbench.simulator.protocols.hbft.message.CheckpointIMessage;

@Component
@ToString
public class CheckpointMessageMutatorFactory extends MessageMutatorFactory {
    RuntimeException invalidMessageTypeException = new IllegalArgumentException("Invalid message type");

    @Override
    public List<MessageMutationFault> mutators() {
        return List.of(
                new MessageMutationFault("hbft-checkpointI-different-digest", "Change digest", List.of(CheckpointMessage.class)) {
                    @Override
                    public void accept(FaultContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof CheckpointIMessage message)) {
                            throw invalidMessageTypeException;
                        }
                        // Create a random digest
                        byte[] digest = new byte[20];
                        CheckpointMessage mutatedMessage = message.withDigest(digest);
                        mutatedMessage.sign(message.getSignedBy());
                        messageEvent.setPayload(mutatedMessage);
                    }
                },
                new MessageMutationFault("hbft-checkpointI-seq-inc", "Increment Sequence Number", List.of(CheckpointMessage.class)) {
                    @Override
                    public void accept(FaultContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof CheckpointIMessage message)) {
                            throw invalidMessageTypeException;
                        }

                        CheckpointMessage mutatedMessage = message.withLastSeqNumber(message.getLastSeqNumber() + 1);
                        mutatedMessage.sign(message.getSignedBy());
                        messageEvent.setPayload(mutatedMessage);
                    }
                },
                new MessageMutationFault("hbft-checkpointI-seq-dec", "Decrement Sequence Number", List.of(CheckpointMessage.class)) {
                    @Override
                    public void accept(FaultContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof CheckpointIMessage message)) {
                            throw invalidMessageTypeException;
                        }
                        CheckpointMessage mutatedMessage = message.withLastSeqNumber(message.getLastSeqNumber() - 1);
                        mutatedMessage.sign(message.getSignedBy());
                        messageEvent.setPayload(mutatedMessage);
                    }
                }
        );
    }
}
