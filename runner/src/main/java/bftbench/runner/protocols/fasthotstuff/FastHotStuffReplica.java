package bftbench.runner.protocols.fasthotstuff;

import bftbench.runner.Replica;
import bftbench.runner.protocols.fasthotstuff.message.*;
import bftbench.runner.transport.MessagePayload;
import bftbench.runner.transport.Transport;
import lombok.Getter;
import lombok.extern.java.Log;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Log
public class FastHotStuffReplica extends Replica<Block> {
    // TODO: Timeout

    @Getter
    private final AtomicLong round = new AtomicLong(3);

    @Getter
    private final AtomicLong lastVotedRound = new AtomicLong(2);

    @Getter
    private final AtomicLong preferredRound = new AtomicLong(1);
    private final AtomicLong highestQcRound = new AtomicLong(2);
    private final NodeStorage storage = new NodeStorage(this);
    private QuorumCertificate highestQc = new QuorumCertificate(null);

    public FastHotStuffReplica(String nodeId, Set<String> nodeIds, Transport transport) {
        super(nodeId, nodeIds, transport);
        createGenesisBlocks();

        if (this.getNodeId().equals(this.getLeader())) {
            System.out.println("OH! IM THE LEADER: " + this.getNodeId());
            this.broadcastMessage(new Block(highestQc, this.round.get(), this.getNodeId(), null));
        }
    }

    private void createGenesisBlocks() {
        List<String> nodeIds = this.getNodeIds().stream().sorted().collect(Collectors.toList());
        // Genesis Block
        Block b0 = new Block(null, 0, nodeIds.get(0), null);
        QuorumCertificate qc0 = new QuorumCertificate(
                nodeIds.stream()
                        .map(nodeId -> new VoteMessage(nodeId, hash(b0).array()))
                        .collect(Collectors.toUnmodifiableList()));
        Block b1 = new Block(qc0, 1, nodeIds.get(1), b0);
        QuorumCertificate qc1 = new QuorumCertificate(
                nodeIds.stream()
                        .map(nodeId -> new VoteMessage(nodeId, hash(b1).array()))
                        .collect(Collectors.toUnmodifiableList()));
        Block b2 = new Block(qc1, 2, nodeIds.get(2), b1);
        QuorumCertificate qc2 = new QuorumCertificate(
                nodeIds.stream()
                        .map(nodeId -> new VoteMessage(nodeId, hash(b2).array()))
                        .collect(Collectors.toUnmodifiableList()));
        // TODO: Add blocks to storage of all replicas!!
        this.storage.addBlock(b0);
        this.storage.addBlock(b1);
        this.storage.addBlock(b2);
        this.storage.commit(b0);
        this.storage.commit(b1); //??
        this.storage.commit(b2); //??
        this.commitOperation(b0);
        this.commitOperation(b1); //??
        this.commitOperation(b2); //??

        this.highestQc = qc2;

        System.out.println("BLOCKS DONE!!");

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
                Block block = this.storage.getBlock(ByteBuffer.wrap(qc.get().getVotes().stream().findAny().get().getBlockHash()));
                this.broadcastMessage(block);
            }
            return;
        }

        if (message instanceof NewViewMessage newView) {
            Optional<AggregateQuorumCertificate> qc = this.storage.addNewView(newView);
            if (qc.isPresent()) {
                Block block = this.storage.getBlock(ByteBuffer.wrap(qc.get().getNewViews().stream().findAny().get().getBlockHash()));
                this.broadcastMessage(block);
            }
            return;
        }

        // Else, unknown message
        log.warning(String.format("Received unknown message from %s: %s", sender, message));
    }

    public void processQuorumCertificate(QuorumCertificate qc) {
        log.info(String.format("Received QC: %s", qc));

        // get block hash from qc
        ByteBuffer blockHash = ByteBuffer.wrap(qc.getVotes().stream().findAny().get().getBlockHash());
        Block block = this.storage.getBlock(blockHash);

        // Get the 2 ancestors of the block
        // b0 <- b1 <- block
        Block b1 = this.storage.getParentBlock(block);
        Block b0 = this.storage.getParentBlock(b1);

        // update the preferred round
        this.preferredRound.set(Math.max(this.preferredRound.get(), b1.getRound()));

        // update the highest QC
        if (b1.getRound() > this.highestQcRound.get()) {
            this.highestQcRound.set(block.getRound());

            //this.highestQc.setVotes(block.getQc().getVotes());
            this.highestQc = block.getQc();
        }

        // update the committed sequence
        this.storage.commit(b0);
        log.info(String.format("Committing block %s", b0));
    }

    public void processBlock(Block block) {
        // get parent block
        Block prevBlock = this.storage.getParentBlock(block);

        // check if we can vote from the block
        if (!this.getNodeIds().contains(block.getAuthor())) {
            log.info(String.format("Block %s not from a valid author", block));
            return;
        }
        if (block.getRound() <= this.lastVotedRound.get()) {
            log.info(String.format("Block %s round is not greater than last voted round", block));
            return;
        }
        if (prevBlock.getRound() < this.preferredRound.get()) {
            log.info(String.format("Block %s round is not greater than preferred round", block));
            return;
        }

        // this.timeout = DELAY;
        this.lastVotedRound.set(block.getRound());
        this.round.set(Math.max(this.round.get(), block.getRound() + 1));
        VoteMessage vote = new VoteMessage(this.getNodeId(), this.hash(block).array());
        String nextLeaderId = this.getLeader((int) block.getRound() + 1);
        this.sendMessage(vote, nextLeaderId);
    }

    public String getLeader(int roundNumber) {
        return this.getNodeIds().stream().sorted().skip(roundNumber % this.getNodeIds().size()).findFirst().get();
    }

    public String getLeader() {
        return this.getLeader((int) this.round.get());
    }

    public boolean verifyBlock(Block block) {
        // author is a valid node id
        // qc is valid
        boolean authorValid = this.getNodeIds().contains(block.getAuthor());
        boolean qcAuthorsValid = block.getQc().getVotes().stream().allMatch(vote -> this.getNodeIds().contains(vote.getAuthor()));
        boolean hasQuorum = block.getQc().getVotes().size() >= this.computeQuorumSize();
        Set<ByteBuffer> committedBlocks = block.getQc().getVotes().stream().map(vote -> ByteBuffer.wrap(vote.getBlockHash())).collect(java.util.stream.Collectors.toSet());
        boolean allSameHash = committedBlocks.size() == 1;
        return authorValid && qcAuthorsValid && allSameHash && hasQuorum;
    }

    public int computeQuorumSize() {
        // FIXME: This may easily break if we start considering clients as nodes!!
        int n = this.getNodeIds().size();
        return (int) Math.ceil((double) (2 * n - 1) / 3);
    }

    public ByteBuffer hash(Block block) {
        return ByteBuffer.wrap(this.digest(block));
    }
}
