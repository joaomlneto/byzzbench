package byzzbench.simulator.protocols.event_driven_hotstuff;

import lombok.Getter;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Objects;

@Getter
public class EDHSNode {
    private String parentHash;
    private ClientRequest clientRequest;
    private EDHSQuorumCertificate justify;
    private long height;
    private String hash;

    public EDHSNode(String parentHash, ClientRequest clientRequest, EDHSQuorumCertificate justify, long height) throws NoSuchAlgorithmException {
        this.parentHash = parentHash;
        this.clientRequest = clientRequest;
        this.justify = justify;
        this.height = height;

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        String nodeString = parentHash + clientRequest + justify.toString() + height;
        byte[] hashBytes = digest.digest(nodeString.getBytes(StandardCharsets.UTF_8));
        this.hash = Base64.getEncoder().encodeToString(hashBytes);
    }

    public boolean isChildOf(EDHSNode parent) {
        return this.parentHash.equals(parent.hash);
    }

    public boolean isExtensionOf(EDHSNode ancestor, HashMap<String, EDHSNode> nodes) {
        return isChildOf(ancestor) ||
                (nodes.containsKey(parentHash) && nodes.get(parentHash).isExtensionOf(ancestor, nodes));
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        EDHSNode node = (EDHSNode) o;
        return Objects.equals(hash, node.hash);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(hash);
    }
}
