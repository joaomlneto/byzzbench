package byzzbench.simulator.protocols.fab;

import byzzbench.simulator.protocols.fab.messages.*;
import byzzbench.simulator.protocols.fab.replica.Pair;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class MessageLog {
    private SortedMap<String, Pair> acceptorsWithAcceptedProposal;
    private SortedMap<String, Pair> proposersWithLearnedValue;
    private Pair learnedValue;
    private List<SignedResponse> responses;

    private final Deque<ProposeMessage> proposeMessages = new ConcurrentLinkedDeque<>();
    private final Map<Long, Ticket> tickets = new ConcurrentHashMap<>();
    private final Map<Long, Collection<AcceptMessage>> acceptMessages = new ConcurrentHashMap<>();
    private final Map<Long, Collection<LearnMessage>> learnMessages = new ConcurrentHashMap<>();
    private final Map<Long, Collection<SatisfiedMessage>> satisfiedMessages = new ConcurrentHashMap<>();
    private final Map<Long, Collection<ReplyMessage>> replyMessages = new ConcurrentHashMap<>();

}
