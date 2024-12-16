package byzzbench.simulator.protocols.event_driven_hotstuff;

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

    private final HashSet<Long> newViewMessages;
    private final HashMap<String, HashSet<PartialSignature>> signatures;
    private boolean done;
    private boolean madeQC;

    public LeaderViewState(EDHotStuffReplica replica, long viewNumber) {
        this.replica = replica;
        this.pacemaker = replica.getPacemaker();
        this.viewNumber = viewNumber;
        signatures = new HashMap<>();
        newViewMessages = new HashSet<>();
        done = false;
        madeQC = false;
    }

    public void addNewViewMessage(NewViewMessage message) {
        if(message.getViewNumber() == viewNumber) newViewMessages.add(message.getViewNumber());
        if(hasNewViewQuorum()) pacemaker.onNewViewQuorum();
    }

    public boolean hasNewViewQuorum() {
        return newViewMessages.size() >= replica.getMinValidVotes();
    }

    public void addSignature(PartialSignature signature) {
        // TODO: make sure we have processed the node we collect votes on
        if(!signatures.containsKey(signature.proposedNodeHash)) signatures.put(signature.proposedNodeHash, new HashSet<>());
        HashSet<PartialSignature> signatureSet = signatures.get(signature.proposedNodeHash);

        signatureSet.add(signature);

        makeQCIfReady();
    }

    public void setViewActionDone() {
        done = true;
    }

    public boolean hasMadeQC() { return madeQC; }

    public HashSet<PartialSignature> getQuorumVotes() {
        List<HashSet<PartialSignature>> quorumSets = signatures.values().stream().filter(signaturesSet -> signaturesSet.size() >= replica.getMinValidVotes()).toList();
        if(quorumSets.isEmpty()) return null;

        return quorumSets.getFirst();
    }

    private void makeQCIfReady() {
        if(madeQC) return;

        HashSet<PartialSignature> quorumVotes = getQuorumVotes();
        if (quorumVotes != null) {
            String quorumNodeHash = quorumVotes.iterator().next().getProposedNodeHash();
            EDHSQuorumCertificate newQC = new EDHSQuorumCertificate(quorumNodeHash, new QuorumSignature(quorumVotes));
            pacemaker.updateHighQC(newQC);
            madeQC = true;

            pacemaker.onNewQC();
        }
    }
}
