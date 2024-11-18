package byzzbench.simulator.protocols.tendermint;

import byzzbench.simulator.LeaderBasedProtocolReplica;
import byzzbench.simulator.protocols.tendermint.message.*;
import byzzbench.simulator.state.SerializableLogEntry;
import byzzbench.simulator.state.TotalOrderCommitLog;
import byzzbench.simulator.transport.MessagePayload;
import byzzbench.simulator.transport.Transport;
import lombok.Getter;
import lombok.extern.java.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.SortedSet;
import java.util.concurrent.atomic.AtomicLong;

@Log
public class TendermintReplica extends LeaderBasedProtocolReplica {

    @Getter
    private final long timeout;

    private final AtomicLong height = new AtomicLong(1);
    private final AtomicLong round = new AtomicLong(0);
    private String lockedValue = null;
    private long lockedRound = -1;
    private String validValue = null;
    private long validRound = -1;

    public TendermintReplica(String nodeId, SortedSet<String> nodeIds, long timeout, Transport transport) {
        super(nodeId, nodeIds, transport, new TotalOrderCommitLog());
        this.timeout = timeout;
    }

    @Override
    public void initialize() {
        log.info("Initializing Tendermint Replica: " + this.getNodeId());
        this.startNewHeight();
    }

    public void startNewHeight() {
        log.info("Starting new height: " + height.get());
        this.round.set(0);
        propose();
    }

    private void propose() {
        if (isProposer()) {
            String proposal = validValue != null ? validValue : createProposal();
            ProposalMessage proposalMessage = new ProposalMessage(height.get(), round.get(), proposal);
            this.broadcastMessage(proposalMessage);
        }
    }

    private boolean isProposer() {
        int proposerIndex = (int) (round.get() % this.getNodeIds().size());
        return this.getNodeId().equals(new ArrayList<>(this.getNodeIds()).get(proposerIndex));
    }

    private String createProposal() {
        return "Block_" + height.get() + "_Round_" + round.get();
    }

    public void recvProposal(ProposalMessage proposal) {
        log.info(String.format("Received Proposal: %s", proposal.getBlock()));
        PrevoteMessage prevoteMessage = proposal.getBlock().equals(lockedValue) || lockedRound < proposal.getRound()
                ? new PrevoteMessage(height.get(), round.get(), proposal.getBlock())
                : new PrevoteMessage(height.get(), round.get(), null);
        this.broadcastMessage(prevoteMessage);
    }

    public void recvPrevote(PrevoteMessage prevote) {
        log.info(String.format("Received Prevote: %s", prevote.getBlock()));
        if (prevote.getBlock() != null && countPrevotes(prevote.getBlock()) >= quorumSize()) {
            lockedValue = prevote.getBlock();
            lockedRound = round.get();
            PrecommitMessage precommitMessage = new PrecommitMessage(height.get(), round.get(), prevote.getBlock());
            this.broadcastMessage(precommitMessage);
        }
    }

    public void recvPrecommit(PrecommitMessage precommit) {
        log.info(String.format("Received Precommit: %s", precommit.getBlock()));
        if (precommit.getBlock() != null && countPrecommits(precommit.getBlock()) >= quorumSize()) {
            commit(precommit.getBlock());
            this.startNewHeight();
        } else {
            this.round.incrementAndGet();
            this.propose();
        }
    }

    private void commit(String block) {
        log.info(String.format("Committing Block: %s", block));
        this.getCommitLog().add(new SerializableLogEntry(block, height.get()));
        this.height.incrementAndGet();
        this.lockedValue = null;
        this.lockedRound = -1;
    }

    private int quorumSize() {
        return (this.getNodeIds().size() * 2) / 3 + 1;
    }

    private int countPrevotes(String block) {
        return quorumSize();
    }

    private int countPrecommits(String block) {
        return quorumSize();
    }

    @Override
    public void handleClientRequest(String clientId, Serializable request) {
        // Tendermint does not directly process client requests
    }

    @Override
    public void handleMessage(String sender, MessagePayload message) {
        if (message instanceof ProposalMessage proposal) {
            recvProposal(proposal);
        } else if (message instanceof PrevoteMessage prevote) {
            recvPrevote(prevote);
        } else if (message instanceof PrecommitMessage precommit) {
            recvPrecommit(precommit);
        } else {
            log.warning("Unknown message type received: " + message.getType());
        }
    }
}
