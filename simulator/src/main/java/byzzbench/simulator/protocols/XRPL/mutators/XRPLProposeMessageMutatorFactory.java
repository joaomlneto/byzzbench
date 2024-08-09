package byzzbench.simulator.protocols.XRPL.mutators;

import byzzbench.simulator.protocols.XRPL.messages.XRPLProposeMessage;
import byzzbench.simulator.transport.MessageMutator;
import byzzbench.simulator.transport.MessageMutatorFactory;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Component
public class XRPLProposeMessageMutatorFactory extends MessageMutatorFactory {
    RuntimeException invalidMessageTypeException = new IllegalArgumentException("Invalid message type");

    @Override
    public List<MessageMutator> mutators() {
        return List.of(
            new MessageMutator("xrpl-propose-proposal-inc", "Increment Proposal Seq", List.of(XRPLProposeMessage.class)) {
                @Override
                public Serializable apply(Serializable serializable) {
                    if (serializable instanceof XRPLProposeMessage message) {
                        return message.withProp(message.getProposal().withSeq(message.getProposal().getSeq() + 1));
                    }
                    throw invalidMessageTypeException;
                }
            },
            new MessageMutator("xrpl-propose-proposal-dec", "Decrement Proposal Seq", List.of(XRPLProposeMessage.class)) {
                @Override
                public Serializable apply(Serializable serializable) {
                    if (serializable instanceof XRPLProposeMessage message) {
                        return message.withProp(message.getProposal().withSeq(message.getProposal().getSeq() - 1));
                    }
                    throw invalidMessageTypeException;
                }
            },
            new MessageMutator("xrpl-propose-mutate-tx", "Mutate Tx", List.of(XRPLProposeMessage.class)) {
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
