package byzzbench.simulator.protocols.event_driven_hotstuff;

import lombok.Getter;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Objects;

@Getter
public class Node {
    private String parentHash;
    private ClientCommand command;
    private QuorumCertificate justify;
    private int height;
    private String hash;

    public Node(String parentHash, ClientCommand command, QuorumCertificate justify, int height) throws NoSuchAlgorithmException {
        this.parentHash = parentHash;
        this.command = command;
        this.justify = justify;
        this.height = height;

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        String nodeString = parentHash + command + justify.toString() + height;
        byte[] hashBytes = digest.digest(nodeString.getBytes(StandardCharsets.UTF_8));
        this.hash = Base64.getEncoder().encodeToString(hashBytes);
    }

    public boolean isChildOf(Node parent) {
        return this.parentHash.equals(parent.hash);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return Objects.equals(hash, node.hash);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(hash);
    }
}
