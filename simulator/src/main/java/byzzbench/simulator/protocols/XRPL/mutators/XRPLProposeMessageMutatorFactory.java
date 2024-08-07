package byzzbench.simulator.protocols.XRPL.mutators;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import byzzbench.simulator.protocols.XRPL.messages.XRPLProposeMessage;
import byzzbench.simulator.transport.MessageMutator;
import byzzbench.simulator.transport.MessageMutatorFactory;

public class XRPLProposeMessageMutatorFactory extends MessageMutatorFactory {
    RuntimeException invalidMessageTypeException = new IllegalArgumentException("Invalid message type");

    @Override
    public List<MessageMutator> mutators() {
        return List.of(
            new MessageMutator("Increment Proposal Seq", List.of(XRPLProposeMessage.class)) {
                @Override
                public Serializable apply(Serializable serializable) {
                    if (serializable instanceof XRPLProposeMessage message) {
                        return message.withProp(message.getProposal().withSeq(message.getProposal().getSeq() + 1));
                    }
                    throw invalidMessageTypeException;
                }
            },
            new MessageMutator("Decrement Proposal Seq", List.of(XRPLProposeMessage.class)) {
                @Override
                public Serializable apply(Serializable serializable) {
                    if (serializable instanceof XRPLProposeMessage message) {
                        return message.withProp(message.getProposal().withSeq(message.getProposal().getSeq() - 1));
                    }
                    throw invalidMessageTypeException;
                }
            },
            new MessageMutator("Mutate Tx", List.of(XRPLProposeMessage.class)) {
                @Override
                public Serializable apply(Serializable serializable) {
                    if (serializable instanceof XRPLProposeMessage message) {
                        List<String> newTxns = new ArrayList<>(); 
                        message.getProposal().getTxns().forEach(tx -> {
                            newTxns.add(tx + "01");
                        });
                        return message.withProp(message.getProposal().withTxns(newTxns));
                    }
                    throw invalidMessageTypeException;
                }
            }
        );
    }

}
