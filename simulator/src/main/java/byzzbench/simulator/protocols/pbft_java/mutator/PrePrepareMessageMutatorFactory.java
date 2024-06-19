package byzzbench.simulator.protocols.pbft_java.mutator;

import byzzbench.simulator.protocols.pbft_java.message.PrePrepareMessage;
import byzzbench.simulator.transport.MessageMutator;
import byzzbench.simulator.transport.MessageMutatorFactory;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@ToString
public class PrePrepareMessageMutatorFactory extends MessageMutatorFactory {
    RuntimeException invalidMessageTypeException = new IllegalArgumentException("Invalid message type");

    @Override
    public List<MessageMutator> mutators() {
        return List.of(

                new MessageMutator("Increment View Number", List.of(PrePrepareMessage.class)) {
                    @Override
                    public Serializable apply(Serializable serializable) {
                        if (serializable instanceof PrePrepareMessage message) {
                            return message.withViewNumber(message.getViewNumber() + 1);
                        }
                        throw invalidMessageTypeException;
                    }
                },
                new MessageMutator("Decrement View Number", List.of(PrePrepareMessage.class)) {
                    @Override
                    public Serializable apply(Serializable serializable) {
                        if (serializable instanceof PrePrepareMessage message) {
                            return message.withViewNumber(message.getViewNumber() - 1);
                        }
                        throw invalidMessageTypeException;
                    }
                },
                new MessageMutator("Increment Sequence Number", List.of(PrePrepareMessage.class)) {
                    @Override
                    public Serializable apply(Serializable serializable) {
                        if (serializable instanceof PrePrepareMessage message) {
                            return message.withSequenceNumber(message.getSequenceNumber() + 1);
                        }
                        throw invalidMessageTypeException;
                    }
                },
                new MessageMutator("Decrement Sequence Number", List.of(PrePrepareMessage.class)) {
                    @Override
                    public Serializable apply(Serializable serializable) {
                        if (serializable instanceof PrePrepareMessage message) {
                            return message.withSequenceNumber(message.getSequenceNumber() - 1);
                        }
                        throw invalidMessageTypeException;
                    }
                }
        );
    }
}
