package byzzbench.simulator.protocols.XRPL;


public class XRPLHeartbeatRunnable implements Runnable {
    XRPLReplica node;

    public XRPLHeartbeatRunnable(XRPLReplica node_) {
        this.node = node_;
    }
    @Override
    public void run() {
        node.onHeartbeat();
    }

}
