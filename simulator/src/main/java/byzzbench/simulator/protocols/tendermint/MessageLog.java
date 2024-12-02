package byzzbench.simulator.protocols.tendermint;

import byzzbench.simulator.protocols.tendermint.message.*;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

import java.util.*;

@Log
@RequiredArgsConstructor
public class MessageLog {
    private final TendermintReplica node;
    private final SortedSet<GenericMessage> Messages = new TreeSet<>();
    @Getter
    private final SortedMap<Block, Long> prevotes = new TreeMap<>();
    @Getter
    private final SortedMap<Block, Long> precommits = new TreeMap<>();

    public static final Block NULL_BLOCK = new Block(Long.MIN_VALUE, Long.MIN_VALUE, Long.MIN_VALUE, "NULL VALUE");


    @Getter
    private long proposalCount = 0;


    public boolean addMessage(GenericMessage voteMessage) {
        log.info("Adding message: " + voteMessage);
        log.info(Messages.contains(voteMessage) + "");
        log.info(Messages.toString());
        boolean added = Messages.add(voteMessage);
        if (added) {
            if (voteMessage instanceof PrecommitMessage) {
                Block block = voteMessage.getBlock();
                if (block == null){
                    block = NULL_BLOCK;
                }
                if(precommits.containsKey(block)) {
                    precommits.put(block, precommits.get(block) + 1);
                } else {
                    precommits.put(block, 1L);
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
        log.info("Checking if block has enough prevotes: " + block);
        log.info(prevotes.getOrDefault(block, 0L).toString());
        log.info("Tolerance: " + node.getTolerance());
        return prevotes.getOrDefault(block, 0L) >= 2 * node.getTolerance() + 1;
    }

    public boolean hasEnoughPreCommits(Block block) {
        log.info("Checking if block has enough precommits: " + block);
        log.info(precommits.getOrDefault(block, 0L).toString());
        log.info("Tolerance: " + node.getTolerance());
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
