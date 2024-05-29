package byzzbench.runner.protocols.fasthotstuff;

import byzzbench.runner.protocols.fasthotstuff.message.NewViewMessage;
import byzzbench.runner.protocols.fasthotstuff.message.VoteMessage;
import lombok.RequiredArgsConstructor;

import java.util.*;

@RequiredArgsConstructor
public class MessageLog {
    private final FastHotStuffReplica node;
    private final Set<Object> committed = new HashSet<>();
    private final Map<Object, Set<Object>> votes = new HashMap<>();
    private final Map<Object, Set<Object>> newViews = new HashMap<>();

    private Collection<Object> canMakeQc(Map<Object, Set<Object>> collection, Object key, Object value) {
        boolean before = collection.containsKey(key) && collection.get(key).size() >= computeQuorumSize();
        collection.computeIfAbsent(key, k -> new HashSet<>()).add(value);
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
