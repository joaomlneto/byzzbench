package byzzbench.simulator.protocols.fasthotstuff;

import byzzbench.simulator.Replica;
import byzzbench.simulator.protocols.fasthotstuff.message.*;
import byzzbench.simulator.state.TotalOrderCommitLog;
import byzzbench.simulator.transport.MessagePayload;
import byzzbench.simulator.transport.Transport;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.extern.java.Log;

import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

@Log
public class FastHotStuffReplica extends Replica<Block> {
    // TODO: Timeout

    @JsonIgnore
    private final AtomicLong round = new AtomicLong(3);
    @JsonIgnore
    private final AtomicLong lastVotedRound = new AtomicLong(2);
    @JsonIgnore
    private final AtomicLong preferredRound = new AtomicLong(1);
    @JsonIgnore
    private final AtomicLong highestQcRound = new AtomicLong(2);

    @JsonIgnore
    private final NodeStorage storage = new NodeStorage(this);

    @JsonIgnore
    private QuorumCertificate highestQc = new QuorumCertificate(null);

    public FastHotStuffReplica(String nodeId, Set<String> nodeIds, Transport transport) {
        super(nodeId, nodeIds, transport, new TotalOrderCommitLog<Block>());
        createGenesisBlocks();

        if (this.getNodeId().equals(this.getLeader())) {
            this.broadcastMessageIncludingSelf(
                    new Block(highestQc,
                            this.round.get(),
                            this.getNodeId(),
                            String.format("%s%d", this.getNodeId(), this.round.get())));
        }
    }

    private void createGenesisBlocks() {
        List<String> nodeIds = this.getNodeIds().stream().sorted().toList();

        // Genesis Block
        Block b0 = new Block(null, 0, nodeIds.get(0), "Genesis Block");
        QuorumCertificate qc0 = new QuorumCertificate(
                nodeIds.stream()
                        .map(nodeId -> new VoteMessage(nodeId, hash(b0)))
                        .toList());

        Block b1 = new Block(qc0, 1, nodeIds.get(1), "B1");
        QuorumCertificate qc1 = new QuorumCertificate(
                nodeIds.stream()
                        .map(nodeId -> new VoteMessage(nodeId, hash(b1)))
                        .toList());

        Block b2 = new Block(qc1, 2, nodeIds.get(2), "B2");
        QuorumCertificate qc2 = new QuorumCertificate(
                nodeIds.stream()
                        .map(nodeId -> new VoteMessage(nodeId, hash(b2)))
                        .toList());

        // if I am node 0, print the blocks and quorum certificates
        if (this.getNodeId().equals(nodeIds.get(0))) {
            log.severe(String.format("Block 0: %s", b0));
            log.severe(String.format("QC 0: %s", qc0));
            log.severe(String.format("Block 1: %s", b1));
            log.severe(String.format("QC 1: %s", qc1));
            log.severe(String.format("Block 2: %s", b2));
            log.severe(String.format("QC 2: %s", qc2));
        }

        // TODO: Add blocks to storage of all replicas!!
        this.storage.addBlock(b0);
        this.storage.addBlock(b1);
        this.storage.addBlock(b2);
        // unsure if the following is required
        //this.storage.addVote(qc0.getVotes().stream().findAny().get());
        //this.storage.addVote(qc1.getVotes().stream().findAny().get());
        //this.storage.addVote(qc2.getVotes().stream().findAny().get());

        this.highestQc = qc2;
        this.highestQcRound.set(2);

        log.info("Initial blocks committed!");

    }

    @Override
    public void handleMessage(String sender, MessagePayload message) throws Exception {
        log.info(String.format("Received message from %s: %s", sender, message));

        // handle incoming blocks
        if (message instanceof Block block) {
            this.storage.addBlock(block);
            this.processQuorumCertificate(block.getQc());
            this.processBlock(block);
            return;
        }

        if (message instanceof VoteMessage vote) {
            Optional<QuorumCertificate> qc = this.storage.addVote(vote);
            if (qc.isPresent()) {
                this.processBlock(this.storage.getBlock(vote.getBlockHash()));
                //Block block = this.storage.getBlock(qc.get().getVotes().stream().findAny().get().getBlockHash());
                Block block = new Block(qc.get(), this.round.get(), this.getNodeId(), String.format("%s%d", this.getNodeId(), this.round.get()));
                this.broadcastMessageIncludingSelf(block);
            }
            return;
        }

        if (message instanceof NewViewMessage newView) {
            Optional<? extends GenericQuorumCertificate> qc = this.storage.addVote(newView);
            if (qc.isPresent()) {
                this.processBlock(this.storage.getBlock(newView.getBlockHash()));
                // FIXME: THIS IS PROBABLY WRONG.
                // see: https://github.com/asonnino/twins-simulator/blob/master/fhs/node.py#L43
                Block block = this.storage.getBlock(qc.get().getVotes().stream().findAny().get().getBlockHash());
                this.broadcastMessageIncludingSelf(block);
            }
            return;
        }

        // Else, unknown message
        log.warning(String.format("Received unknown message from %s: %s", sender, message));
    }

    public void processQuorumCertificate(QuorumCertificate qc) {
        log.info(String.format("Received QC: %s", qc));

        // get block hash from qc
        String blockHash = qc.getVotes().stream().findAny().get().getBlockHash();
        Block block = this.storage.getBlock(blockHash);

        // Get the 2 ancestors of the block
        // b0 <- b1 <- block
        Block b1 = this.storage.getParentBlock(block);
        Block b0 = this.storage.getParentBlock(b1);

        // print the hashes of the three blocks: b0, b1 and block
        log.info(String.format("Block 0: %s", b0));
        log.info(String.format("Block 1: %s", b1));
        log.info(String.format("Block: %s", block));

        // update the preferred round
        this.preferredRound.set(Math.max(this.preferredRound.get(), b1.getRound()));

        // update the highest QC
        if (b1.getRound() > this.highestQcRound.get()) {
            this.highestQc = block.getQc();
            this.highestQcRound.set(b1.getRound());
        }

        // update the committed sequence
        this.storage.commit(b0);
        log.info(String.format("Committing block %s", b0));
    }

    public void processBlock(Block block) {
        // get parent block
        Block prevBlock = this.storage.getParentBlock(block);

        // check if we can vote from the block
        // check if the block is from a valid author
        if (!this.getNodeIds().contains(block.getAuthor())) {
            log.warning(String.format("Block %s not from a valid author", block));
            return;
        }
        // check if the round is greater than the last voted round
        if (block.getRound() <= this.lastVotedRound.get()) {
            log.warning(String.format("Block %s round is not greater than last voted round", block));
            return;
        }
        // check if the previous block's round is greater or equal to the preferred round
        if (prevBlock != null && prevBlock.getRound() < this.preferredRound.get()) {
            log.warning(String.format("Block %s parent's round is smaller than preferred round", block));
            return;
        }

        // TODO: Timeouts
        // this.timeout = DELAY;
        this.lastVotedRound.set(block.getRound());
        this.round.set(Math.max(this.round.get(), block.getRound() + 1));
        VoteMessage vote = new VoteMessage(this.getNodeId(), this.hash(block));
        String nextLeaderId = this.getLeader(block.getRound() + 1);
        log.info(String.format("Sending vote %s to next leader: %s", vote, nextLeaderId));
        this.sendMessage(vote, nextLeaderId);
    }

    public String getLeader(long roundNumber) {
        //return this.getNodeIds().stream().sorted().skip(roundNumber % this.getNodeIds().size()).findFirst().get();
        // sorted nodeIDS
        List<String> nodeIds = this.getNodeIds().stream().sorted().toList();
        // num nodes
        int n = nodeIds.size();
        // get roundNumber-th node
        return nodeIds.get((int) (roundNumber % n));
    }

    public String getLeader() {
        return this.getLeader(this.round.get());
    }

    public boolean verifyBlock(Block block) {
        // author is a valid node id
        // qc is valid
        boolean authorValid = this.getNodeIds().contains(block.getAuthor());
        boolean qcAuthorsValid = block.getQc().getVotes().stream().allMatch(vote -> this.getNodeIds().contains(vote.getAuthor()));
        boolean hasQuorum = block.getQc().getVotes().size() >= this.computeQuorumSize();
        Set<String> committedBlocks = block.getQc().getVotes().stream().map(vote -> vote.getBlockHash()).collect(java.util.stream.Collectors.toSet());
        boolean allSameHash = committedBlocks.size() == 1;
        return authorValid && qcAuthorsValid && allSameHash && hasQuorum;
    }

    public int computeQuorumSize() {
        // FIXME: This may easily break if we start considering clients as nodes!!
        int n = this.getNodeIds().size();
        return (int) Math.ceil((double) (2 * n - 1) / 3);
    }

    public String hash(Block block) {
        return Base64.getEncoder().encodeToString(this.digest(block));
    }
}
