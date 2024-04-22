package bftbench.runner.pbft.mutator;

import bftbench.runner.pbft.message.PrePrepareMessage;
import bftbench.runner.transport.MessageMutator;
import bftbench.runner.transport.MessageMutatorFactory;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@ToString
public class PrePrepareMessageMutatorFactory<T> extends MessageMutatorFactory<PrePrepareMessage<T>> {

    public Class<? extends Serializable> getSerializableClass() {
        return PrePrepareMessage.class;
    }

    @Override
    public List<MessageMutator<PrePrepareMessage<T>>> mutators() {
        return List.of(
                new MessageMutator<PrePrepareMessage<T>>() {
                    @Override
                    public Serializable apply(PrePrepareMessage<T> message) {
                        return new PrePrepareMessage<T>(
                                message.getViewNumber() + 1,
                                message.getSequenceNumber(),
                                message.getDigest(),
                                message.getRequest()
                        );
                    }

                    @Override
                    public String name() {
                        return "incrementViewNumber";
                    }
                },

                new MessageMutator<PrePrepareMessage<T>>() {
                    @Override
                    public Serializable apply(PrePrepareMessage<T> message) {
                        return new PrePrepareMessage<T>(
                                message.getViewNumber() - 1,
                                message.getSequenceNumber(),
                                message.getDigest(),
                                message.getRequest()
                        );
                    }

                    @Override
                    public String name() {
                        return "decrementViewNumber";
                    }
                },

                new MessageMutator<PrePrepareMessage<T>>() {
                    @Override
                    public Serializable apply(PrePrepareMessage<T> message) {
                        return new PrePrepareMessage<T>(
                                message.getViewNumber(),
                                message.getSequenceNumber() + 1,
                                message.getDigest(),
                                message.getRequest()
                        );
                    }

                    @Override
                    public String name() {
                        return "incrementSequenceNumber";
                    }
                },

                new MessageMutator<PrePrepareMessage<T>>() {
                    @Override
                    public Serializable apply(PrePrepareMessage<T> message) {
                        return new PrePrepareMessage<T>(
                                message.getViewNumber(),
                                message.getSequenceNumber() - 1,
                                message.getDigest(),
                                message.getRequest()
                        );
                    }

                    @Override
                    public String name() {
                        return "decrementSequenceNumber";
                    }
                }
        );
    }
}
