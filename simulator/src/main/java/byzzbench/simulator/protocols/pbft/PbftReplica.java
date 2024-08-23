package byzzbench.simulator.protocols.pbft;

import byzzbench.simulator.Replica;
import byzzbench.simulator.state.CommitLog;
import byzzbench.simulator.transport.MessagePayload;
import byzzbench.simulator.transport.Transport;
import java.util.Random;
import java.util.SortedSet;

public abstract class PbftReplica extends Replica {
  protected Random random = new Random();

  // !! From Node.cc !!

  // max faulty nodes
  protected long numFaulty;
  // num principals
  protected long numPrincipals;

  // current view number
  protected long v = 0;

  // current primary
  protected String primary;

  // !! end from Node.cc !!

  protected long seqno = 0;
  protected long lastStable = 0;
  protected long lowBound = 0;
  protected long lastPrepared = 0;
  protected long lastExecuted = 0;
  protected long lastTentativeExecute = 0;
  protected long lastStatus = 0;
  protected boolean limbo = false;
  protected boolean hasNvState = false;
  protected long nbreqs = 0;
  protected long nbrounds = 0;
  protected boolean recovering = false;
  protected long qs = 0;
  protected long rr = 0;
  protected long[] rrViews;
  protected long recoveryPoint = Integer.MAX_VALUE;
  protected long maxRecN = 0;
  protected long execCommand = 0;
  protected long nonDetChoices = 0;

  protected PbftReplica(String nodeId, SortedSet<String> nodeIds,
                        Transport transport, CommitLog commitLog) {
    super(nodeId, nodeIds, transport, commitLog);

    this.numFaulty = (nodeIds.size() - 1) / 3;
    this.numPrincipals =
        nodeIds.size(); // FIXME: not yet being used? all are principals
    this.primary = nodeIds.stream().sorted().findFirst().get();

    this.initialize();
  }

  @Override
  public void initialize() {
    // TODO: Timers (Replica.cc, lines 135-147)
    this.rrViews = new long[(int)this.numPrincipals]; // line 152
  }

  @Override
  public void handleMessage(String sender, MessagePayload message)
      throws Exception {}
}
