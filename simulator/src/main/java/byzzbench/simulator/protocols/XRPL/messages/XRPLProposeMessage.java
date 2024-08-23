package byzzbench.simulator.protocols.XRPL.messages;

import byzzbench.simulator.protocols.XRPL.XRPLProposal;
import byzzbench.simulator.transport.MessagePayload;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

/*
 * Node Proposal message sent in the establish phase.
 * A node sends this message when it is trying to propose
 * a ledger for the given sequence number, a node may update
 * its proposal via other propose messages with subsequent
 * sequence numbers.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@With
public class XRPLProposeMessage extends MessagePayload {
  private XRPLProposal proposal;
  private String senderId;

  public XRPLProposeMessage(XRPLProposal prop_, String id) {
    this.proposal = prop_;
    this.senderId = id;
  }

  @Override
  public String getType() {
    return "PROPOSE";
  }
}
