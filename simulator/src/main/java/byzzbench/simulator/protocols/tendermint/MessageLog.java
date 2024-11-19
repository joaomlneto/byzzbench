package byzzbench.simulator.protocols.tendermint;

import byzzbench.simulator.protocols.fasthotstuff.FastHotStuffReplica;
import byzzbench.simulator.protocols.fasthotstuff.message.NewViewMessage;
import byzzbench.simulator.protocols.fasthotstuff.message.VoteMessage;
import lombok.RequiredArgsConstructor;

import java.util.*;

@RequiredArgsConstructor
public class MessageLog {
    private final FastHotStuffReplica node;
    private final SortedSet<Object> committed = new TreeSet<>();
    private final SortedMap<Object, SortedSet<Object>> votes = new TreeMap<>();
    private final SortedMap<Object, SortedSet<Object>> newViews = new TreeMap<>();

    private Collection<Object> canMakeQc(SortedMap<Object, SortedSet<Object>> collection, Object key, Object value) {
        boolean before = collection.containsKey(key) && collection.get(key).size() >= computeQuorumSize();
        collection.computeIfAbsent(key, k -> new TreeSet<>()).add(value);
        boolean after = collection.containsKey(key) && collection.get(key).size() >= computeQuorumSize();
        return after && !before ? collection.get(key) : null;
    }

    public void addVote(VoteMessage vote) {
        String digest = vote.getBlockHash();
        //Set<Object> votes = this.
    }

    public void addVote(NewViewMessage newView) {

    }

    private int computeQuorumSize() {
        int n = this.node.getNodeIds().size();
        return (int) Math.ceil(2 * n / 3);
    }
}
