package byzzbench.simulator.protocols.XRPL.mutators;

import byzzbench.simulator.faults.FaultInput;
import byzzbench.simulator.faults.MessageMutationFault;
import byzzbench.simulator.faults.MessageMutatorFactory;
import byzzbench.simulator.protocols.XRPL.messages.XRPLProposeMessage;
import byzzbench.simulator.transport.Event;
import byzzbench.simulator.transport.MessageEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class XRPLProposeMessageMutatorFactory
    extends MessageMutatorFactory<Serializable> {
  RuntimeException invalidMessageTypeException =
      new IllegalArgumentException("Invalid message type");

  @Override
  public List<MessageMutationFault<Serializable>> mutators() {
    return List.of(
        new MessageMutationFault<>("xrpl-propose-proposal-inc",
                                   "Increment Proposal Seq",
                                   List.of(XRPLProposeMessage.class)) {
          @Override
          public void accept(FaultInput<Serializable> serializable) {
            Optional<Event> event = serializable.getEvent();
            if (event.isEmpty()) {
              throw invalidMessageTypeException;
            }
            if (!(event.get() instanceof MessageEvent messageEvent)) {
              throw invalidMessageTypeException;
            }
            if (!(messageEvent.getPayload() instanceof
                  XRPLProposeMessage message)) {
              throw invalidMessageTypeException;
            }
            message.getProposal().setSeq(message.getProposal().getSeq() + 1);
          }
        },
        new MessageMutationFault<>("xrpl-propose-proposal-dec",
                                   "Decrement Proposal Seq",
                                   List.of(XRPLProposeMessage.class)) {
          @Override
          public void accept(FaultInput<Serializable> serializable) {
            Optional<Event> event = serializable.getEvent();
            if (event.isEmpty()) {
              throw invalidMessageTypeException;
            }
            if (!(event.get() instanceof MessageEvent messageEvent)) {
              throw invalidMessageTypeException;
            }
            if (!(messageEvent.getPayload() instanceof
                  XRPLProposeMessage message)) {
              throw invalidMessageTypeException;
            }
            message.getProposal().setSeq(message.getProposal().getSeq() - 1);
          }
        },
        new MessageMutationFault<>("xrpl-propose-mutate-tx", "Mutate Tx",
                                   List.of(XRPLProposeMessage.class)) {
          @Override
          public void accept(FaultInput<Serializable> serializable) {
            Optional<Event> event = serializable.getEvent();
            if (event.isEmpty()) {
              throw invalidMessageTypeException;
            }
            if (!(event.get() instanceof MessageEvent messageEvent)) {
              throw invalidMessageTypeException;
            }
            if (!(messageEvent.getPayload() instanceof
                  XRPLProposeMessage message)) {
              throw invalidMessageTypeException;
            }
            List<String> newTxns = new ArrayList<>();
            message.getProposal().getTxns().forEach(
                tx -> { newTxns.add(tx + "01"); });
            message.getProposal().setTxns(newTxns);
          }
        });
  }
}
