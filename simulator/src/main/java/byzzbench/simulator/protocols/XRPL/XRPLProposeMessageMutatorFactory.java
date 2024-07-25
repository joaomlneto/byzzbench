package byzzbench.simulator.protocols.XRPL;

import byzzbench.simulator.protocols.XRPL.messages.XRPLProposeMessage;
import byzzbench.simulator.transport.MessageMutator;
import byzzbench.simulator.transport.MessageMutatorFactory;
import java.io.Serializable;
import java.util.List;

public class XRPLProposeMessageMutatorFactory extends MessageMutatorFactory {
  RuntimeException invalidMessageTypeException =
      new IllegalArgumentException("Invalid message type");

  @Override
  public List<MessageMutator> mutators() {
    return List.of(
        new MessageMutator("Increment Proposal Seq",
                           List.of(XRPLProposeMessage.class)) {
          @Override
          public Serializable apply(Serializable serializable) {
            if (serializable instanceof XRPLProposeMessage message) {
              return message.withProp(message.getProposal().withSeq(
                  message.getProposal().getSeq() + 1));
            }
            throw invalidMessageTypeException;
          }
        },
        new MessageMutator("Decrement Proposal Seq",
                           List.of(XRPLProposeMessage.class)) {
          @Override
          public Serializable apply(Serializable serializable) {
            if (serializable instanceof XRPLProposeMessage message) {
              return message.withProp(message.getProposal().withSeq(
                  message.getProposal().getSeq() - 1));
            }
            throw invalidMessageTypeException;
          }
        });
  }
}
