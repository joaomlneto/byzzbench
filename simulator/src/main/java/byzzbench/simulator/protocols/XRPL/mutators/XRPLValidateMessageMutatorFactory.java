package byzzbench.simulator.protocols.XRPL.mutators;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import byzzbench.simulator.protocols.XRPL.messages.XRPLValidateMessage;
import byzzbench.simulator.transport.MessageMutator;
import byzzbench.simulator.transport.MessageMutatorFactory;

public class XRPLValidateMessageMutatorFactory extends MessageMutatorFactory {

    @Override
    public List<MessageMutator> mutators() {
        RuntimeException invalidMessageTypeException = new IllegalArgumentException("Invalid message type");
        return List.of(
            new MessageMutator("change tx", List.of(XRPLValidateMessage.class)) {

                @Override
                public Serializable apply(Serializable t) {
                    if (t instanceof XRPLValidateMessage message) {
                        List<String> newTxns = new ArrayList<>();
                        for (String tx : message.getLedger().getTransactions()) {
                            newTxns.add(tx + "01");
                        }
                        return message.withLedger(message.getLedger().withTransactions(newTxns));
                    }
                    throw invalidMessageTypeException;
                }                
            }
        );
    }

}
