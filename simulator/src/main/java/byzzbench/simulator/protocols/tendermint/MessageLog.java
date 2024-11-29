package byzzbench.simulator.protocols.tendermint;

import byzzbench.simulator.protocols.tendermint.message.GenericMessage;

import byzzbench.simulator.protocols.tendermint.message.PrecommitMessage;
import byzzbench.simulator.protocols.tendermint.message.PrevoteMessage;
import byzzbench.simulator.protocols.tendermint.message.ProposalMessage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.*;

@RequiredArgsConstructor
public class MessageLog {
    private final TendermintReplica node;
    private final SortedSet<GenericMessage> Messages = new TreeSet<>();

    @Getter
    private long proposalCount = 0;

    @Getter
    private long precommitCount = 0;

    @Getter
    private long prevoteCount = 0;


    public boolean addMessage(GenericMessage voteMessage) {
        boolean added = Messages.add(voteMessage);
        if (voteMessage instanceof PrecommitMessage) {
            precommitCount++;
        } else if (voteMessage instanceof PrevoteMessage) {
            prevoteCount++;
        }
        else if (voteMessage instanceof ProposalMessage) {
            proposalCount++;
        }
        return added;
    }

    public long getMessageCount() {
        return Messages.size();
    }

    public boolean hasEnoughPreVotes(PrevoteMessage prevoteMessage) {
        return prevoteCount >= 2 * node.getTolerance() + 1;
    }

    public boolean hasEnoughPreCommits(PrecommitMessage precommitMessage) {
        return precommitCount >= 2 * node.getTolerance() + 1;
    }

    public boolean contains(PrevoteMessage prevoteMessage) {
        return Messages.contains(prevoteMessage);
    }

    public void sentPrevote() {
        prevoteCount++;
    }

    public void sentProposal() {
        proposalCount++;
    }

    public void sentPrecommit() {
        precommitCount++;
    }
}
