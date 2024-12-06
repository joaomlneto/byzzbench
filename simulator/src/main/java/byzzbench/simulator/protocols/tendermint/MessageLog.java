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

    // Stores all generic messages
    @Getter
    private final SortedSet<GenericMessage> messages = new TreeSet<>();

    @Getter
    private final SortedMap<Block, List<PrevoteMessage>> prevotes = new TreeMap<>();
    @Getter
    private final SortedMap<Block, List<PrecommitMessage>> precommits = new TreeMap<>();
    @Getter
    private final SortedMap<Block, List<ProposalMessage>> proposals = new TreeMap<>();

    public static final Block NULL_BLOCK = new Block(Long.MIN_VALUE, Long.MIN_VALUE, Long.MIN_VALUE, "NULL VALUE");

    @Getter
    private long proposalCount = 0;
    @Getter
    private long prevotesCount = 0;
    @Getter
    private long precommitCount = 0;

    /**
     * Adds a message to the message log, maintaining the appropriate mappings.
     */
    public boolean addMessage(GenericMessage voteMessage) {
        boolean added = messages.add(voteMessage); // Add to the global set of messages
        if (added) {
            Block block = voteMessage.getBlock() == null ? NULL_BLOCK : voteMessage.getBlock();

            // Handle different types of messages
            switch (voteMessage) {
                case PrevoteMessage prevoteMessage -> {
                    prevotes.computeIfAbsent(block, k -> new ArrayList<>()).add(prevoteMessage);
                    prevotesCount++; // Increment prevote count
                }
                case PrecommitMessage precommitMessage -> {
                    precommits.computeIfAbsent(block, k -> new ArrayList<>()).add(precommitMessage);
                    precommitCount++; // Increment precommit count
                }
                case ProposalMessage proposalMessage -> {
                    proposals.computeIfAbsent(block, k -> new ArrayList<>()).add(proposalMessage);
                    proposalCount++; // Increment proposal count
                }
                default -> {
                }
            }
        }
        return added;
    }

    /**
     * Returns the total number of messages stored.
     */
    public long getMessageCount() {
        return messages.size();
    }

    /**
     * Checks if a block has enough prevotes.
     */
    public boolean hasEnoughPreVotes(Block block) {
        return prevotes.getOrDefault(block, Collections.emptyList()).size() >= 2 * node.getTolerance() + 1;
    }

    /**
     * Checks if a block has enough precommits.
     */
    public boolean hasEnoughPreCommits(Block block) {
        return precommits.getOrDefault(block, Collections.emptyList()).size() >= 2 * node.getTolerance() + 1;
    }

    /**
     * Checks if a specific prevote message exists.
     */
    public boolean contains(PrevoteMessage prevoteMessage) {
        return messages.contains(prevoteMessage);
    }

    /**
     * Gets the count of prevote messages for a specific block.
     */
    public int getPrevoteCount(Block block) {
        return prevotes.getOrDefault(block, Collections.emptyList()).size();
    }

    /**
     * Checks if there are f+1 messages in a specific round after a given round.
     */
    public boolean fPlus1MessagesInRound(GenericMessage message, long round) {
        return messages.stream()
                .filter(m -> m.getRound() > round && m.getRound() == message.getRound())
                .count() >= node.getTolerance() + 1;
    }
}
