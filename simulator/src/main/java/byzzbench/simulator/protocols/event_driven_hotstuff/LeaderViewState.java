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
    private final HashMap<String, HashMap<PartialSignature, GenericVote>> votes;
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
        replica.log("Processing NEW-VIEW message for view " + message.getViewNumber());

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
        replica.log("Processing vote for node " + vote.getNode().getHash() + " with height " + vote.getNode().getHeight() + " with signature for node " + vote.getPartialSignature().getProposedNodeHash() + " signed by " + vote.getPartialSignature().getSenderId());


        String nodeHash = vote.getNode().getHash();
        if(!votes.containsKey(nodeHash)) votes.put(nodeHash, new HashMap<>());
        HashMap<PartialSignature, GenericVote> voteSet = votes.get(nodeHash);

        voteSet.put(vote.getPartialSignature(), vote);

        makeQCIfReady();
    }

    public void setViewActionDone() {
        done = true;
    }

    public boolean hasMadeQC() { return madeQC; }

    public HashSet<GenericVote> getQuorumVotes() {
        List<HashMap<PartialSignature, GenericVote>> quorumSets = votes.values().stream().filter(votesSet -> votesSet.size() >= replica.getMinValidVotes()).toList();
        if(quorumSets.isEmpty()) return null;

        return new HashSet<>(quorumSets.getFirst().values());
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
            replica.log("Created new QC for node " + quorumNodeHash + " with height " + voteNode.getHeight());
            replica.log("Quorum votes size: " + quorumVotes.size());
            replica.log("signatures size: " + signatures.size());
            replica.log("QC signatures: " + signatures.stream().map(s -> "signature for " + s.getProposedNodeHash() + " from " + s.getSenderId()).toList());
            pacemaker.updateHighQC(newQC);
            madeQC = true;

            pacemaker.onNewQC();
        }
    }
}
