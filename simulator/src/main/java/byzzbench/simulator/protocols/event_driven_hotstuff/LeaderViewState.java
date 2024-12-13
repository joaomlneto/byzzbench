package byzzbench.simulator.protocols.event_driven_hotstuff;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;

import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.List;

@Getter
public class LeaderViewState {
    @JsonIgnore
    private final transient EDHotStuffReplica replica;
    private final long viewNumber;

    private EDHSNode previousNode;
    private final HashSet<PartialSignature> signatures;
    private boolean done;

    public LeaderViewState(EDHotStuffReplica replica, long viewNumber) {
        this.replica = replica;
        this.viewNumber = viewNumber;
        signatures = new HashSet<>();
        done = false;
    }

    private boolean hasPreviousNode() {
        return previousNode != null;
    }

    public void setPreviousNode(EDHSNode previousNode) {
        if (hasPreviousNode()) return;
        this.previousNode = previousNode;

        createQCIfReady();
    }

    public void addSignature(PartialSignature signature) {
        signatures.add(signature);

        createQCIfReady();
    }

    public boolean isReady() {
        if (!hasPreviousNode()) return false;
        List<PartialSignature> filteredSignatures = signatures.stream().filter(s -> s.getProposedNodeHash().equals(previousNode.getHash())).toList();

        return filteredSignatures.size() >= replica.getMinValidVotes();
    }

    public boolean isAbleToSend() {
        return (!isDone()) && isReady();
    }

    public void setViewActionDone() {
        done = true;
    }

    private void createQCIfReady() {
        if (isReady()) {
            String nodeHash = previousNode.getHash();
            QuorumCertificate newQC = new QuorumCertificate(nodeHash, new QuorumSignature(signatures));
            replica.updateHighQC(newQC);

            try {
                replica.onBeat();
            } catch (NoSuchAlgorithmException ignored) {
            }
        }
    }
}
