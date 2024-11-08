package byzzbench.simulator.protocols.XRPL.mutators;

import byzzbench.simulator.faults.FaultContext;
import byzzbench.simulator.faults.factories.MessageMutatorFactory;
import byzzbench.simulator.faults.faults.MessageMutationFault;
import byzzbench.simulator.protocols.XRPL.messages.XRPLSubmitMessage;
import byzzbench.simulator.transport.Event;
import byzzbench.simulator.transport.MessageEvent;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class XRPLSubmitMessageMutatorFactory extends MessageMutatorFactory {
  RuntimeException invalidMessageTypeException =
      new IllegalArgumentException("Invalid message type");

  @Override
  public List<MessageMutationFault> mutators() {
    return List.of(new MessageMutationFault("xrpl-submit-change-tx",
                                            "Change tx",
                                            List.of(XRPLSubmitMessage.class)) {
      @Override
      public void accept(FaultContext t) {
        Optional<Event> event = t.getEvent();
        if (event.isEmpty()) {
          throw invalidMessageTypeException;
        }
        if (!(event.get() instanceof MessageEvent messageEvent)) {
          throw invalidMessageTypeException;
        }
        if (!(messageEvent.getPayload() instanceof XRPLSubmitMessage message)) {
          throw invalidMessageTypeException;
        }
        XRPLSubmitMessage mutatedMessage =
            message.withTx(message.getTx() + "01");
        mutatedMessage.sign(message.getSignedBy());
        messageEvent.setPayload(mutatedMessage);
      }
    });
  }
}
