package byzzbench.simulator.protocols.fasthotstuff;

import byzzbench.simulator.LeaderBasedProtocolReplica;
import byzzbench.simulator.Scenario;
import byzzbench.simulator.protocols.fasthotstuff.message.*;
import byzzbench.simulator.state.TotalOrderCommitLog;
import byzzbench.simulator.transport.MessagePayload;
import lombok.Getter;
import lombok.extern.java.Log;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Log
@Getter
public class FastHotStuffReplica extends LeaderBasedProtocolReplica {
    public static final Duration TIMEOUT_DELAY = Duration.ofSeconds(15);

    private final AtomicLong round = new AtomicLong(3);
    private final AtomicLong lastVotedRound = new AtomicLong(2);
    private final AtomicLong preferredRound = new AtomicLong(1);
    private final AtomicLong highestQcRound = new AtomicLong(2);
    private final SortedMap<String, SortedSet<VoteMessage>> votes = new TreeMap<>();
    private final SortedMap<Long, SortedSet<NewViewMessage>> newViews = new TreeMap<>();
    private final SortedMap<String, Block> knownBlocks = new TreeMap<>();
    private GenericQuorumCertificate highestQc = new QuorumCertificate(new ArrayList<>());

    public FastHotStuffReplica(String nodeId, Scenario scenario) {
        super(nodeId, scenario, new TotalOrderCommitLog());
    }

    @Override
    public void initialize() {
        // create genesis blocks for the first 3 rounds
        createGenesisBlocks();

        // leader broadcasts a block
        if (this.getId().equals(this.getLeader())) {
            this.broadcastMessageIncludingSelf(
                    new Block(highestQc,
                            this.round.get(),
                            this.getId(),
                            String.format("%s%d", this.getId(), this.round.get())));
        }

        // create 15 second timeout
        this.resetTimeout();
    }

    private void createGenesisBlocks() {
        List<String> nodeIds = this.getNodeIds().stream().sorted().toList();

        // Genesis Block
        Block b0 = new Block(null, 0, this.getLeader(0), "Genesis Block");
        QuorumCertificate qc0 = new QuorumCertificate(
                nodeIds.stream()
                        .map(nodeId -> new VoteMessage(nodeId, hash(b0)))
                        .toList());

        Block b1 = new Block(qc0, 1, this.getLeader(1), "B1");
        QuorumCertificate qc1 = new QuorumCertificate(
                nodeIds.stream()
                        .map(nodeId -> new VoteMessage(nodeId, hash(b1)))
                        .toList());

        Block b2 = new Block(qc1, 2, this.getLeader(2), "B2");
        QuorumCertificate qc2 = new QuorumCertificate(
                nodeIds.stream()
                        .map(nodeId -> new VoteMessage(nodeId, hash(b2)))
                        .toList());

        // if I am node 0, print the blocks and quorum certificates
        if (this.getId().equals(nodeIds.get(0))) {
            log.severe(String.format("Block 0: %s", b0));
            log.severe(String.format("QC 0: %s", qc0));
            log.severe(String.format("Block 1: %s", b1));
            log.severe(String.format("QC 1: %s", qc1));
            log.severe(String.format("Block 2: %s", b2));
            log.severe(String.format("QC 2: %s", qc2));
        }

        this.addBlock(b0);
        this.addBlock(b1);
        this.addBlock(b2);

        this.highestQc = qc2;
        this.highestQcRound.set(2);

        log.info("Initial blocks committed!");

    }

    @Override
    public void handleMessage(String sender, MessagePayload message) {
        log.info(String.format("Received message from %s: %s", sender, message));

        // handle incoming blocks
        if (message instanceof Block block) {
            this.addBlock(block);
            this.processQuorumCertificate(block.getQc());
            this.processBlock(block);
            return;
        }

        if (message instanceof VoteMessage vote) {
            Optional<QuorumCertificate> qc = this.addVote(vote);
            if (qc.isPresent()) {
                this.processBlock(this.getBlock(vote.getBlockHash()));
                Block block = new Block(qc.get(), this.round.get(), this.getId(), String.format("%s%d", this.getId(), this.round.get()));
                this.broadcastMessageIncludingSelf(block);
            }
            return;
        }

        if (message instanceof NewViewMessage newView) {
            Optional<AggregateQuorumCertificate> qc = this.addVote(newView);
            if (qc.isPresent()) {
                this.processBlock(this.getBlock(newView.getBlockHash()));
                // FIXME: THIS IS PROBABLY WRONG.
                // see: https://github.com/asonnino/twins-simulator/blob/master/fhs/node.py#L43
                // XXX: Now this is probably right!
                Block block = new Block(qc.get(), this.round.get(), this.getId(), String.format("%s%d", this.getId(), this.round.get()));
                this.broadcastMessageIncludingSelf(block);
            }
            return;
        }

        // Else, unknown message
        log.warning(String.format("Received unknown message from %s: %s", sender, message));
    }

    public void processQuorumCertificate(GenericQuorumCertificate qc) {
        log.info(String.format("Received QC: %s", qc));

        // get block hash from qc
        String blockHash = qc.getVotes().stream().findAny().get().getBlockHash();
        Block block = this.getBlock(blockHash);

        // Get the 2 ancestors of the block
        // b0 <- b1 <- block
        Block b1 = this.getParentBlock(block);
        Block b0 = this.getParentBlock(b1);

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
        this.commit(block.getRound(), b0);
        log.info(String.format("Committing block %s", b0));
    }

    public void processBlock(Block block) {
        // get parent block
        Block prevBlock = this.getParentBlock(block);

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

        this.resetTimeout();
        this.lastVotedRound.set(block.getRound());
        this.round.set(Math.max(this.round.get(), block.getRound() + 1));
        VoteMessage vote = new VoteMessage(this.getId(), this.hash(block));
        String nextLeaderId = this.getLeader(block.getRound() + 1);
        log.info(String.format("Sending vote %s to next leader: %s", vote, nextLeaderId));
        this.sendMessage(vote, nextLeaderId);
    }

    public String getLeader(long roundNumber) {
        // sorted nodeIDS
        List<String> nodeIds = this.getNodeIds().stream().sorted().toList();

        // XXX: This is a hack to make the leader selection deterministic to reproduce the FHS bug
        List<Integer> bugOrder = List.of(0, 1, 0, 0, 1, 0, 2, 1, 1, 2, 2);
        if (roundNumber < bugOrder.size()) {
            return nodeIds.get(bugOrder.get((int) roundNumber));
        }

        // get roundNumber-th node
        return nodeIds.get((int) (roundNumber % nodeIds.size()));
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
        SortedSet<String> committedBlocks = block.getQc().getVotes().stream().map(vote -> vote.getBlockHash()).collect(Collectors.toCollection(TreeSet::new));
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

    public void handleTimeout() {
        log.info("Timeout!");
        this.round.incrementAndGet();
        this.resetTimeout();
        NewViewMessage vote = new NewViewMessage(this.highestQc, this.round.get(), this.getId());
        String nextLeader = this.getLeader();
        this.sendMessage(vote, nextLeader);
        log.info("Sending new view message to next leader: " + nextLeader);
    }

    public void resetTimeout() {
        this.clearAllTimeouts();
        this.setTimeout("Timeout", this::handleTimeout, TIMEOUT_DELAY);
    }


    // Adds a block to the storage
    public void addBlock(Block block) {
        this.knownBlocks.put(this.hash(block), block);
    }

    // Returns the block with the given hash
    public Block getBlock(String hash) {
        return this.knownBlocks.get(hash);
    }

    // Returns the parent block of the given block
    public Block getParentBlock(Block block) {
        return this.getBlock(block.getParentHash());
    }

    // Commits a block
    public void commit(long seqNumber, Block block) {
        System.out.println("COMITTING BLOCK: " + block);
        // Check if the parent is known
        if (block.getParentHash() != null && knownBlocks.get(block.getParentHash()) == null) {
            throw new IllegalArgumentException("Cannot commit block: parent not found");
        }

        /*
        // Check if the parent is committed
        if (block.getParentHash() != null && !committedBlocks.contains(block.getParentHash())) {
            throw new IllegalArgumentException("Cannot commit block: parent not committed");
        }*/

        this.commitOperation(seqNumber, block);
    }

    // Adds a vote to the storage
    public Optional<QuorumCertificate> addVote(VoteMessage message) {
        String digest = message.getBlockHash();
        Optional<SortedSet<VoteMessage>> votes = canMakeQc(this.votes, digest, message);
        if (votes.isPresent()) {
            return Optional.of(new QuorumCertificate(votes.get()));
        } else {
            return Optional.empty();
        }
    }

    public Optional<AggregateQuorumCertificate> addVote(NewViewMessage message) {
        long round = message.getRound();
        Optional<SortedSet<NewViewMessage>> newViewsQc = canMakeQc(this.newViews, round, message);
        if (newViewsQc.isPresent()) {
            return Optional.of(new AggregateQuorumCertificate(newViewsQc.get()));
        } else {
            return Optional.empty();
        }
    }

    public <K, V extends GenericVoteMessage> Optional<SortedSet<V>> canMakeQc(Map<K, SortedSet<V>> collection, K key, V value) {
        boolean before = collection.containsKey(key) && collection.get(key).size() >= this.computeQuorumSize();
        collection.computeIfAbsent(key, k -> new TreeSet<>()).add(value);
        boolean after = collection.containsKey(key) && collection.get(key).size() >= this.computeQuorumSize();
        if (after && !before) {
            return Optional.of(collection.get(key));
        } else {
            return Optional.empty();
        }
    }
}
