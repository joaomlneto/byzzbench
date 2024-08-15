package byzzbench.simulator.protocols.XRPL.mutators;

import byzzbench.simulator.faults.FaultInput;
import byzzbench.simulator.faults.MessageMutationFault;
import byzzbench.simulator.faults.MessageMutatorFactory;
import byzzbench.simulator.protocols.XRPL.messages.XRPLValidateMessage;
import byzzbench.simulator.transport.Event;
import byzzbench.simulator.transport.MessageEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class XRPLValidateMessageMutatorFactory extends MessageMutatorFactory {

  @Override
  public List<MessageMutationFault<Serializable>> mutators() {
    RuntimeException invalidMessageTypeException =
        new IllegalArgumentException("Invalid message type");
    return List.of(new MessageMutationFault<>(
        "change tx", "Change TX", List.of(XRPLValidateMessage.class)) {
      @Override
      public void accept(FaultInput<Serializable> serializable) {
        Optional<Event> event = serializable.getEvent();
        if (event.isEmpty()) {
          throw invalidMessageTypeException;
        }
        if (!(event.get() instanceof MessageEvent messageEvent)) {
          throw invalidMessageTypeException;
        }
        if (messageEvent.getPayload() instanceof XRPLValidateMessage message) {
          List<String> newTxns = new ArrayList<>();
          for (String tx : message.getLedger().getTransactions()) {
            newTxns.add(tx + "01");
          }
          message.getLedger().setTransactions(newTxns);
        }
        throw invalidMessageTypeException;
      }
    });
  }
}
