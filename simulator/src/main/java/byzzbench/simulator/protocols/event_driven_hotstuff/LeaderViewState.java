package byzzbench.simulator.protocols.event_driven_hotstuff;

import byzzbench.simulator.protocols.event_driven_hotstuff.messages.GenericVote;
import byzzbench.simulator.protocols.event_driven_hotstuff.messages.NewViewMessage;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

@Getter
public class LeaderViewState {
    @JsonIgnore
    private final transient EDHotStuffReplica replica;
    @JsonIgnore
    private final transient EDHSPacemaker pacemaker;
    private final long viewNumber;

    private final HashSet<String> newViewMessages;
    private final HashMap<String, HashSet<GenericVote>> votes;
    private boolean done;
    private boolean madeQC;

    public LeaderViewState(EDHotStuffReplica replica, long viewNumber) {
        this.replica = replica;
        this.pacemaker = replica.getPacemaker();
        this.viewNumber = viewNumber;
        votes = new HashMap<>();
        newViewMessages = new HashSet<>();
        done = false;
        madeQC = false;
    }

    public void onViewStart() {
        checkForNewViewQuorum();
        makeQCIfReady();
    }

    public void addNewViewMessage(NewViewMessage message, String senderId) {
        if(message.getViewNumber() == viewNumber) newViewMessages.add(senderId);
        checkForNewViewQuorum();
    }

    public void checkForNewViewQuorum() {
        if(hasNewViewQuorum() && (replica.getViewNumber() == viewNumber)) pacemaker.onNewViewQuorum();
    }

    public boolean hasNewViewQuorum() {
        return newViewMessages.size() >= replica.getMinValidVotes();
    }

    public void addVote(GenericVote vote) {
        // TODO: make sure we have processed the node we collect votes on

        String nodeHash = vote.getNode().getHash();
        if(!votes.containsKey(nodeHash)) votes.put(nodeHash, new HashSet<>());
        HashSet<GenericVote> voteSet = votes.get(nodeHash);

        voteSet.add(vote);

        makeQCIfReady();
    }

    public void setViewActionDone() {
        done = true;
    }

    public boolean hasMadeQC() { return madeQC; }

    public HashSet<GenericVote> getQuorumVotes() {
        List<HashSet<GenericVote>> quorumSets = votes.values().stream().filter(votessSet -> votessSet.size() >= replica.getMinValidVotes()).toList();
        if(quorumSets.isEmpty()) return null;

        return quorumSets.getFirst();
    }

    private void makeQCIfReady() {
        if(madeQC || (replica.getViewNumber() != viewNumber)) return;

        HashSet<GenericVote> quorumVotes = getQuorumVotes();
        if (quorumVotes != null) {
            EDHSNode voteNode = quorumVotes.iterator().next().getNode();
            String quorumNodeHash = voteNode.getHash();
            if(!replica.hashNodeMap.containsKey(quorumNodeHash)) replica.catchUp(voteNode);
            HashSet<PartialSignature> signatures = new HashSet<>(quorumVotes.stream().map(GenericVote::getPartialSignature).toList());
            EDHSQuorumCertificate newQC = new EDHSQuorumCertificate(quorumNodeHash, new QuorumSignature(signatures));
            pacemaker.updateHighQC(newQC);
            madeQC = true;

            pacemaker.onNewQC();
        }
    }
}
