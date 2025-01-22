package byzzbench.simulator.protocols.tendermint;

import byzzbench.simulator.protocols.tendermint.message.*;

import byzzbench.simulator.transport.DefaultClientRequestPayload;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

import java.util.*;
import java.util.stream.Collectors;

@Log
@RequiredArgsConstructor
public class MessageLog {
    private final TendermintReplica node;

    @Getter
    private final SortedMap<String, SortedSet<GenericMessage>> receivedMessages = new TreeMap<>();

    @Getter
    private final SortedSet<GenericMessage> sentMessages = new TreeSet<>();

    @Getter
    private final SortedMap<Block, List<PrevoteMessage>> prevotes = new TreeMap<>();
    @Getter
    private final SortedMap<Block, List<PrecommitMessage>> precommits = new TreeMap<>();
    @Getter
    private final SortedMap<Block, List<ProposalMessage>> proposals = new TreeMap<>();

    @Getter
    private final Set<RequestMessage> requests = new HashSet<>();

    public static final Block NULL_BLOCK = new Block(Long.MIN_VALUE, "NULL VALUE", null);

    @Getter
    private long proposalCount = 0;
    @Getter
    private long prevotesCount = 0;
    @Getter
    private long precommitCount = 0;

    /**
     * Adds a message to the message log, maintaining the appropriate mappings.
     *
     * @param voteMessage the message to add
     * @return true if the message was added successfully (not seen before), false otherwise
     */
    public boolean addMessage(GenericMessage voteMessage) {
        // Get the set of received messages for the author
        SortedSet<GenericMessage> authorMessages = receivedMessages
                .computeIfAbsent(voteMessage.getAuthor(), k -> new TreeSet<>());

        // Check if the message already exists
        boolean isNewMessage = authorMessages.add(voteMessage);

        if (!isNewMessage) {
            // Message already exists, return false
            return false;
        }

        // Process the message based on its type
        Block block = voteMessage.getBlock() == null ? NULL_BLOCK : voteMessage.getBlock();

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
                // No action needed for other message types
            }
        }

        return true; // Successfully added a new message
    }

    /**
     * Returns the total number of messages stored.
     */
    public long getMessageCount() {
        return receivedMessages.values().stream().mapToLong(Set::size).sum();
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
     * Gets the count of prevote messages for a specific block.
     */
    public int getPrevoteCount(Block block) {
        return prevotes.getOrDefault(block, Collections.emptyList()).size();
    }

    /**
     * Checks if there are f+1 messages in a specific round after a given round.
     */
    public boolean fPlus1MessagesInRound(long height, long round) {
        // Flatten the received messages map and filter by height and round > round
        Map<Long, Long> roundMessageCounts = receivedMessages.values().stream()
                .flatMap(Set::stream) // Flatten all sets of messages into a single stream
                .filter(m -> m.getHeight() == height && m.getRound() > round) // Apply filters
                .collect(Collectors.groupingBy(GenericMessage::getRound, Collectors.counting())); // Group by round and count

        // Check if any round group has at least f + 1 messages
        return roundMessageCounts.values().stream()
                .anyMatch(count -> count >= node.getTolerance() + 1);
    }

    public void bufferRequest(RequestMessage request) {
        requests.add(request);
    }

    public void removeRequest(Block block) {
        Set<RequestMessage> toDelete = requests.stream().filter(r -> r.getOperation() == block.getRequestMessage().getOperation()).collect(Collectors.toSet());
        for (RequestMessage d : toDelete){
            requests.remove(d);
        }
    }

    public void clear(Block block) {
//        messages.clear();
        prevotes.remove(block);
        prevotes.remove(NULL_BLOCK);
        precommits.remove(block);
        precommits.remove(NULL_BLOCK);
        proposals.remove(block);
        precommitCount = 0;
        prevotesCount = 0;
        proposalCount = 0;
    }
}
