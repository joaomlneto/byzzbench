package byzzbench.simulator.protocols.basic_hotstuff;

import lombok.Data;

@Data
public class Node {
    private Node parent;
    private String cmd;
}
