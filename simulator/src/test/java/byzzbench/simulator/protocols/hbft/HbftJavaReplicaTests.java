package byzzbench.simulator.protocols.hbft;

import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert;

import byzzbench.simulator.protocols.hbft.message.CommitMessage;
import byzzbench.simulator.protocols.hbft.message.PrepareMessage;
import byzzbench.simulator.protocols.hbft.message.RequestMessage;
import byzzbench.simulator.protocols.hbft.pojo.ReplicaRequestKey;
import byzzbench.simulator.protocols.hbft.pojo.ReplicaTicketPhase;
import byzzbench.simulator.scheduler.Scheduler;

@SpringBootTest
public class HbftJavaReplicaTests {

    private HbftJavaReplica replica;
    private int tolerance = 1;

    @Test
    void test() {}

    @BeforeEach
    void setup() {
        String replicaId = "A";
        SortedSet<String> nodeIds = new TreeSet<>();
        nodeIds.add("A");
        nodeIds.add("B");
        nodeIds.add("C");
        nodeIds.add("D");
        MessageLog messageLog = new MessageLog(100, 100, 200);
        Scheduler scheduler = Mockito.mock(Scheduler.class);
        /* 
         * We need to have a Scheduler in order to be 
         * able to test message reception and sending
         */
        HbftJavaScenario hbftScenario = new HbftJavaScenario(scheduler);
        replica = new HbftJavaReplica(replicaId, nodeIds, tolerance, 5, messageLog, hbftScenario);

        // Sets the viewNumber to 1 for the replica
        replica.initialize();
    }

    @Test
	void testRecvPrepare() {
        long viewNumber = 1;
        long seqNumber = 1;
        RequestMessage request = new RequestMessage("123", 0, "C0");
        byte[] digest = replica.digest(request);
        PrepareMessage prepare = new PrepareMessage(viewNumber, seqNumber, digest, request);
        
        replica.recvPrepare(prepare);

        // Prepare message should be accepted
        Assert.isTrue(replica.getMessageLog().getTicket(viewNumber, seqNumber).getMessages().contains(prepare), "Prepare message with correct seqNum and viewNum should be accepted!");
    }

    @Test
	void testRecvWrongSeqPrepares() {
        long viewNumber = 1;
        long seqNumber = 2;
        RequestMessage request = new RequestMessage("123", 0, "C0");
        byte[] digest = replica.digest(request);
        PrepareMessage prepare = new PrepareMessage(viewNumber, seqNumber, digest, request);
        PrepareMessage prepare0 = new PrepareMessage(viewNumber, 0, digest, request);
        
        replica.recvPrepare(prepare);
        replica.recvPrepare(prepare0);

        // Prepare message should not be accepted so ticket should be null
        Assert.isTrue(replica.getMessageLog().getTicket(viewNumber, seqNumber) == null, "Prepare messages with incorrect seqNum not accepted!");
        Assert.isTrue(replica.getMessageLog().getTicket(viewNumber, 0) == null, "Prepare messages with incorrect seqNum not accepted!");
    }

    @Test
	void testRecvWrongViewPrepares() {
        long viewNumber = 2;
        long seqNumber = 1;
        RequestMessage request = new RequestMessage("123", 0, "C0");
        byte[] digest = replica.digest(request);
        PrepareMessage prepare = new PrepareMessage(viewNumber, seqNumber, digest, request);
        PrepareMessage prepare0 = new PrepareMessage(0, seqNumber, digest, request);
        
        replica.recvPrepare(prepare);
        replica.recvPrepare(prepare0);

        // Prepare message should not be accepted so ticket should be null
        Assert.isTrue(replica.getMessageLog().getTicket(viewNumber, seqNumber) == null, "Prepare messages with incorrect viewNum not accepted!");
        Assert.isTrue(replica.getMessageLog().getTicket(0, seqNumber) == null, "Prepare messages with incorrect viewNum not accepted!");
    }
    
    @Test
	void testRecvWrongDigestPrepare() {
        long viewNumber = 1;
        long seqNumber = 1;
        RequestMessage request = new RequestMessage("123", 0, "C0");
        RequestMessage request2 = new RequestMessage("321", 100, "C0");
        byte[] digest = replica.digest(request2);
        PrepareMessage prepare = new PrepareMessage(viewNumber, seqNumber, digest, request);
        
        replica.recvPrepare(prepare);

        // Prepare message should not be accepted
        Assert.isTrue(replica.getMessageLog().getTicket(viewNumber, seqNumber).getMessages() == null, "Prepare with incorrect digest should not be accepted!");
    }

    @Test
	void testRecvDiffSeqWithAlreadyAcceptedPrepare() {
        long viewNumber = 1;
        long seqNumber = 1;
        RequestMessage request = new RequestMessage("123", 0, "C0");
        byte[] digest = replica.digest(request);
        PrepareMessage prepare = new PrepareMessage(viewNumber, seqNumber, digest, request);
        PrepareMessage prepare2 = new PrepareMessage(viewNumber, seqNumber + 1, digest, request);
        
        replica.recvPrepare(prepare);

        // Correct prepare should be accepted
        Assert.isTrue(replica.getMessageLog().getTicket(viewNumber, seqNumber).getMessages().contains(prepare), "Accept first prepare!");

        replica.recvPrepare(prepare2);

        // Prepare message should not be accepted
        Assert.isTrue(replica.getMessageLog().getTicket(viewNumber, seqNumber + 1).getMessages() == null, "Correct prepare with already ");
    }

    @Test
	void testRecvCommit() {
        long viewNumber = 1;
        long seqNumber = 1;
        RequestMessage request = new RequestMessage("123", 0, "C0");
        byte[] digest = replica.digest(request);
        //PrepareMessage prepare = new PrepareMessage(viewNumber, seqNumber, digest, request);
        CommitMessage commit = new CommitMessage(viewNumber, seqNumber, digest, request, "B", this.replica.getSpeculativeHistory());

        replica.recvCommit(commit);

        // Commit message should be accepted
        Assert.isTrue(replica.getMessageLog().getTicket(viewNumber, seqNumber).getMessages().contains(commit), "Commit message with correct seqNum and viewNum should be accepted!");
        Assert.isTrue(!replica.getMessageLog().getTicket(viewNumber, seqNumber).isCommittedLocal(tolerance), "This commit should not accept local commit!");
        Assert.isTrue(replica.getMessageLog().getTicket(viewNumber, seqNumber).getPhase() == ReplicaTicketPhase.PREPARE, "Replica should still be in prepare phase!");

        CommitMessage commit2 = new CommitMessage(viewNumber, seqNumber, digest, request, "C", this.replica.getSpeculativeHistory());
        replica.recvCommit(commit2);

        ReplicaRequestKey key = new ReplicaRequestKey("C0", 0);
        Assert.isTrue(replica.getMessageLog().getTicketFromCache(key).isPrepared(tolerance), "Should have received f + 1 commits!");
        Assert.isTrue(replica.getMessageLog().getTicketFromCache(key).isCommittedLocal(tolerance), "Should send commit and thus have 2f + 1 commits!");
    }

    @Test
	void testRecvCommitAfterPrepare() {
        long viewNumber = 1;
        long seqNumber = 1;
        RequestMessage request = new RequestMessage("123", 0, "C0");
        byte[] digest = replica.digest(request);
        PrepareMessage prepare = new PrepareMessage(viewNumber, seqNumber, digest, request);
        CommitMessage commit = new CommitMessage(viewNumber, seqNumber, digest, request, "B", this.replica.getSpeculativeHistory());

        replica.recvPrepare(prepare);
        replica.recvCommit(commit);

        // Commit message should be accepted
        Assert.isTrue(!replica.getMessageLog().getTicket(viewNumber, seqNumber).isCommittedLocal(tolerance), "This commit should not accept local commit!");
        Assert.isTrue(replica.getMessageLog().getTicket(viewNumber, seqNumber).getPhase() == ReplicaTicketPhase.COMMIT, "Replica should be in commit phase!");

        CommitMessage commit2 = new CommitMessage(viewNumber, seqNumber, digest, request, "B", this.replica.getSpeculativeHistory());
        replica.recvCommit(commit2);

        ReplicaRequestKey key = new ReplicaRequestKey("C0", 0);
        Assert.isTrue(replica.getMessageLog().getTicketFromCache(key).isPrepared(tolerance), "Should have received f + 1 commits!");
        Assert.isTrue(replica.getMessageLog().getTicketFromCache(key).isCommittedLocal(tolerance), "Should send commit and thus have 2f + 1 commits!");
    }
}
