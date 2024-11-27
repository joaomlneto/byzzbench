package byzzbench.simulator.protocols.tendermint;

import byzzbench.simulator.protocols.tendermint.message.GenericMessage;

import byzzbench.simulator.protocols.tendermint.message.PrecommitMessage;
import byzzbench.simulator.protocols.tendermint.message.PrevoteMessage;
import lombok.RequiredArgsConstructor;

import java.util.*;

@RequiredArgsConstructor
public class MessageLog {
    private final TendermintReplica node;
    private SortedMap<Long,GenericMessage> voteMessages = new TreeMap<>();

    public void addVoteMessage(GenericMessage voteMessage) {
        voteMessages.put(voteMessage.getHeight(), voteMessage);
    }

    public long getVoteMessageCount() {
        return voteMessages.size();
    }

    public boolean hasEnoughPreVotes(PrevoteMessage prevoteMessage) {
        long prevoteCount = voteMessages.values().stream()
                .filter(voteMessage -> voteMessage instanceof PrevoteMessage)
                .count();
        return prevoteCount >= 2 * node.getTolerance() + 1;
    }

    public boolean hasEnoughPreCommits(PrecommitMessage precommitMessage) {
        long precommitCount = voteMessages.values().stream()
                .filter(voteMessage -> voteMessage instanceof PrecommitMessage)
                .count();
        return precommitCount >= 2 * node.getTolerance() + 1;
    }
}
