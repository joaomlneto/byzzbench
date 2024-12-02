package byzzbench.simulator.protocols.tendermint;

import byzzbench.simulator.protocols.tendermint.message.*;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.*;

@RequiredArgsConstructor
public class MessageLog {
    private final TendermintReplica node;
    private final SortedSet<GenericMessage> Messages = new TreeSet<>();
    @Getter
    private final SortedMap<Block, Long> prevotes = new TreeMap<>();
    @Getter
    private final SortedMap<Block, Long> precommits = new TreeMap<>();

    @Getter
    private long proposalCount = 0;


    public boolean addMessage(GenericMessage voteMessage) {
        if (voteMessage.getBlock()==null) {
            return false;
        }
        boolean added = Messages.add(voteMessage);
        if (added) {
            if (voteMessage instanceof PrecommitMessage) {
                if(precommits.containsKey(voteMessage.getBlock())) {
                    precommits.put(voteMessage.getBlock(), precommits.get(voteMessage.getBlock()) + 1);
                } else {
                    precommits.put(voteMessage.getBlock(), 1L);
                }
            } else if (voteMessage instanceof PrevoteMessage) {
                if(prevotes.containsKey(voteMessage.getBlock())) {
                    prevotes.put(voteMessage.getBlock(), prevotes.get(voteMessage.getBlock()) + 1);
                } else {
                    prevotes.put(voteMessage.getBlock(), 1L);
                }
            } else if (voteMessage instanceof ProposalMessage) {
                proposalCount++;
            }
        }
        return added;
    }

    public long getMessageCount() {
        return Messages.size();
    }

    public boolean hasEnoughPreVotes(Block block) {
        return prevotes.getOrDefault(block, 0L) >= 2 * node.getTolerance() + 1;
    }

    public boolean hasEnoughPreCommits(Block block) {
        return precommits.getOrDefault(block, 0L) >= 2 * node.getTolerance() + 1;
    }

    public boolean contains(PrevoteMessage prevoteMessage) {
        return Messages.contains(prevoteMessage);
    }

    public String getPrevoteCount(Block block) {
        return prevotes.getOrDefault(block, 0L).toString();
    }

//    public void sentPrevote() {
//        prevoteCount++;
//    }
//
//    public void sentProposal() {
//        proposalCount++;
//    }
//
//    public void sentPrecommit() {
//        precommitCount++;
//    }
}
