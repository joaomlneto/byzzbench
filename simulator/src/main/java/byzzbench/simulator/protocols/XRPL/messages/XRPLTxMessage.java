package byzzbench.simulator.protocols.XRPL.messages;

import byzzbench.simulator.transport.MessagePayload;

/*
 * Dummy client request message containing the
 * transaction to be submitted. Transactions are
 * represented by dummy String values.
 */
public class XRPLTxMessage implements MessagePayload {
  private String tx;
  private String senderId;

  public String getSenderId() { return senderId; }
  public XRPLTxMessage(String tx_, String clientId) {
    this.tx = tx_;
    this.senderId = clientId;
  }
  @Override
  public String getType() {
    return "TX";
  }

  public String getTx() { return this.tx; }
}
