package byzzbench.simulator.protocols.pbft_java.mutator;

import byzzbench.simulator.protocols.pbft_java.message.PrePrepareMessage;
import byzzbench.simulator.transport.MessageMutator;
import byzzbench.simulator.transport.MessageMutatorFactory;
import lombok.ToString;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;

@Component
@ToString
public class PrePrepareMessageMutatorFactory extends MessageMutatorFactory {
    RuntimeException invalidMessageTypeException = new IllegalArgumentException("Invalid message type");

    @Override
    public List<MessageMutator> mutators() {
        return List.of(

                new MessageMutator("pbft-preprepare-view-inc", "Increment View Number", List.of(PrePrepareMessage.class)) {
                    @Override
                    public Serializable apply(Serializable serializable) {
                        if (serializable instanceof PrePrepareMessage message) {
                            return message.withViewNumber(message.getViewNumber() + 1);
                        }
                        throw invalidMessageTypeException;
                    }
                },
                new MessageMutator("pbft-preprepare-view-dec", "Decrement View Number", List.of(PrePrepareMessage.class)) {
                    @Override
                    public Serializable apply(Serializable serializable) {
                        if (serializable instanceof PrePrepareMessage message) {
                            return message.withViewNumber(message.getViewNumber() - 1);
                        }
                        throw invalidMessageTypeException;
                    }
                },
                new MessageMutator("pbft-preprepare-seq-inc", "Increment Sequence Number", List.of(PrePrepareMessage.class)) {
                    @Override
                    public Serializable apply(Serializable serializable) {
                        if (serializable instanceof PrePrepareMessage message) {
                            return message.withSequenceNumber(message.getSequenceNumber() + 1);
                        }
                        throw invalidMessageTypeException;
                    }
                },
                new MessageMutator("pbft-preprepare-sec-dec", "Decrement Sequence Number", List.of(PrePrepareMessage.class)) {
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
