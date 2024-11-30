package byzzbench.simulator.protocols.hbft;

import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert;

import byzzbench.simulator.protocols.hbft.message.CheckpointIIMessage;
import byzzbench.simulator.protocols.hbft.message.CheckpointIMessage;
import byzzbench.simulator.protocols.hbft.message.CheckpointMessage;
import byzzbench.simulator.protocols.hbft.message.CommitMessage;
import byzzbench.simulator.protocols.hbft.message.PanicMessage;
import byzzbench.simulator.protocols.hbft.message.PrepareMessage;
import byzzbench.simulator.protocols.hbft.message.RequestMessage;
import byzzbench.simulator.protocols.hbft.message.ViewChangeMessage;
import byzzbench.simulator.protocols.hbft.pojo.ReplicaRequestKey;
import byzzbench.simulator.protocols.hbft.pojo.ReplicaTicketPhase;
import byzzbench.simulator.scheduler.Scheduler;

@SpringBootTest
public class HbftJavaReplicaTests {

    private HbftJavaReplica replicaA;
    private HbftJavaReplica replicaC;
    private HbftJavaReplica replicaD;
    private HbftJavaReplica primary;
    private int tolerance = 1;
    Scheduler scheduler = Mockito.mock(Scheduler.class);

    @Test
    void test() {}

    @BeforeEach
    void setup() {
        String replicaAId = "A";
        String primaryId = "B";
        String replicaCId = "C";
        String replicaDId = "D";
        SortedSet<String> nodeIds = new TreeSet<>();
        nodeIds.add("A");
        nodeIds.add("B");
        nodeIds.add("C");
        nodeIds.add("D");
        MessageLog messageLogA = new MessageLog(100, 100, 200);
        MessageLog messageLogPrimary = new MessageLog(100, 100, 200);
        MessageLog messageLogC = new MessageLog(100, 100, 200);
        MessageLog messageLogD = new MessageLog(100, 100, 200);
        
        /* 
         * We need to have a Scheduler in order to be 
         * able to test message reception and sending
         */
        HbftJavaScenario hbftScenario = new HbftJavaScenario(scheduler);
        replicaA = new HbftJavaReplica<String, String>(replicaAId, nodeIds, tolerance, 5, messageLogA, hbftScenario);
        replicaC = new HbftJavaReplica<String, String>(replicaCId, nodeIds, tolerance, 5, messageLogC, hbftScenario);
        replicaD = new HbftJavaReplica<String, String>(replicaDId, nodeIds, tolerance, 5, messageLogD, hbftScenario);
        primary = new HbftJavaReplica<String, String>(primaryId, nodeIds, tolerance, 5, messageLogPrimary, hbftScenario);

        // Sets the viewNumber to 1 for the replicaA
        replicaA.initialize();
        replicaC.initialize();
        primary.initialize();
    }

    @Test
	void testRecvPrepare() {
        long viewNumber = 1;
        long seqNumber = 1;
        RequestMessage request = new RequestMessage("123", 0, "C0");
        byte[] digest = replicaA.digest(request);
        PrepareMessage prepare = new PrepareMessage(viewNumber, seqNumber, digest, request);
        
        replicaA.recvPrepare(prepare);

        // Prepare message should be accepted
        Assert.isTrue(replicaA.getMessageLog().getTicket(viewNumber, seqNumber).getMessages().contains(prepare), "Prepare message with correct seqNum and viewNum should be accepted!");
    }

    @Test
	void testRecvWrongSeqPrepares() {
        long viewNumber = 1;
        long seqNumber = 2;
        RequestMessage request = new RequestMessage("123", 0, "C0");
        byte[] digest = replicaA.digest(request);
        PrepareMessage prepare = new PrepareMessage(viewNumber, seqNumber, digest, request);
        PrepareMessage prepare0 = new PrepareMessage(viewNumber, 0, digest, request);
        
        replicaA.recvPrepare(prepare);
        replicaA.recvPrepare(prepare0);

        // Prepare message should not be accepted so ticket should be null
        Assert.isTrue(replicaA.getMessageLog().getTicket(viewNumber, seqNumber) == null, "Prepare messages with incorrect seqNum not accepted!");
        Assert.isTrue(replicaA.getMessageLog().getTicket(viewNumber, 0) == null, "Prepare messages with incorrect seqNum not accepted!");
    }

    @Test
	void testRecvWrongViewPrepares() {
        long viewNumber = 2;
        long seqNumber = 1;
        RequestMessage request = new RequestMessage("123", 0, "C0");
        byte[] digest = replicaA.digest(request);
        PrepareMessage prepare = new PrepareMessage(viewNumber, seqNumber, digest, request);
        PrepareMessage prepare0 = new PrepareMessage(0, seqNumber, digest, request);
        
        replicaA.recvPrepare(prepare);
        replicaA.recvPrepare(prepare0);

        // Prepare message should not be accepted so ticket should be null
        Assert.isTrue(replicaA.getMessageLog().getTicket(viewNumber, seqNumber) == null, "Prepare messages with incorrect viewNum not accepted!");
        Assert.isTrue(replicaA.getMessageLog().getTicket(0, seqNumber) == null, "Prepare messages with incorrect viewNum not accepted!");
    }
    
    @Test
	void testRecvWrongDigestPrepare() {
        long viewNumber = 1;
        long seqNumber = 1;
        RequestMessage request = new RequestMessage("123", 0, "C0");
        RequestMessage request2 = new RequestMessage("321", 100, "C0");
        byte[] digest = replicaA.digest(request2);
        PrepareMessage prepare = new PrepareMessage(viewNumber, seqNumber, digest, request);
        
        replicaA.recvPrepare(prepare);

        // Prepare message should not be accepted
        Assert.isTrue(replicaA.getMessageLog().getTicket(viewNumber, seqNumber) == null, "Prepare with incorrect digest should not be accepted!");
    }

    /* @Test
	void testRecvDiffSeqWithAlreadyAcceptedPrepare() {
        long viewNumber = 1;
        long seqNumber = 1;
        RequestMessage request = new RequestMessage("123", 0, "C0");
        byte[] digest = replicaA.digest(request);
        PrepareMessage prepare = new PrepareMessage(viewNumber, seqNumber, digest, request);
        PrepareMessage prepare2 = new PrepareMessage(viewNumber, seqNumber + 1, digest, request);
        
        replicaA.recvPrepare(prepare);

        // Correct prepare should be accepted
        Assert.isTrue(replicaA.getMessageLog().getTicket(viewNumber, seqNumber).getMessages().contains(prepare), "Accept first prepare!");

        replicaA.recvPrepare(prepare2);

        // Prepare message should not be accepted
        Assert.isTrue(replicaA.getMessageLog().getTicket(viewNumber, seqNumber + 1) == null, "Correct prepare with already ");
    } */

    @Test
	void testRecvCommit() {
        long viewNumber = 1;
        long seqNumber = 1;
        RequestMessage request = new RequestMessage("123", 0, "C0");
        byte[] digest = replicaA.digest(request);
        //PrepareMessage prepare = new PrepareMessage(viewNumber, seqNumber, digest, request);
        CommitMessage commit = new CommitMessage(viewNumber, seqNumber, digest, request, "B", this.replicaA.getSpeculativeHistory());

        replicaA.recvCommit(commit);

        // Commit message should be accepted
        Assert.isTrue(replicaA.getMessageLog().getTicket(viewNumber, seqNumber).getMessages().contains(commit), "Commit message with correct seqNum and viewNum should be accepted!");
        Assert.isTrue(!replicaA.getMessageLog().getTicket(viewNumber, seqNumber).isCommittedLocal(tolerance), "This commit should not accept local commit!");
        Assert.isTrue(replicaA.getMessageLog().getTicket(viewNumber, seqNumber).getPhase() == ReplicaTicketPhase.PREPARE, "replicaA should still be in prepare phase!");

        CommitMessage commit2 = new CommitMessage(viewNumber, seqNumber, digest, request, "C", this.replicaA.getSpeculativeHistory());
        replicaA.recvCommit(commit2);

        ReplicaRequestKey key = new ReplicaRequestKey("C0", 0);
        Assert.isTrue(replicaA.getMessageLog().getTicketFromCache(key).isPrepared(tolerance), "Should have received f + 1 commits!");
        Assert.isTrue(replicaA.getMessageLog().getTicketFromCache(key).isCommittedLocal(tolerance), "Should send commit and thus have 2f + 1 commits!");
    }

    @Test
	void testRecvCommitAfterPrepare() {
        long viewNumber = 1;
        long seqNumber = 1;
        RequestMessage request = new RequestMessage("123", 0, "C0");
        byte[] digest = replicaA.digest(request);
        PrepareMessage prepare = new PrepareMessage(viewNumber, seqNumber, digest, request);
        CommitMessage commit = new CommitMessage(viewNumber, seqNumber, digest, request, "B", this.replicaA.getSpeculativeHistory());

        replicaA.recvPrepare(prepare);
        replicaA.recvCommit(commit);

        // Commit message should be accepted
        Assert.isTrue(!replicaA.getMessageLog().getTicket(viewNumber, seqNumber).isCommittedLocal(tolerance), "This commit should not accept local commit!");
        Assert.isTrue(replicaA.getMessageLog().getTicket(viewNumber, seqNumber).getPhase() == ReplicaTicketPhase.COMMIT, "replicaA should be in commit phase!");

        CommitMessage commit2 = new CommitMessage(viewNumber, seqNumber, digest, request, "B", this.replicaA.getSpeculativeHistory());
        replicaA.recvCommit(commit2);

        ReplicaRequestKey key = new ReplicaRequestKey("C0", 0);
        Assert.isTrue(replicaA.getMessageLog().getTicketFromCache(key).isPrepared(tolerance), "Should have received f + 1 commits!");
        Assert.isTrue(replicaA.getMessageLog().getTicketFromCache(key).isCommittedLocal(tolerance), "Should send commit and thus have 2f + 1 commits!");
    }

    @Test
	void testRecvGoodPanic() {
        long viewNumber = 1;
        long seqNumber = 1;
        String clientId = "C0";
        RequestMessage request = new RequestMessage("123", 0, clientId);
        byte[] digest = replicaA.digest(request);
        PrepareMessage prepare = new PrepareMessage(viewNumber, seqNumber, digest, request);
        PanicMessage panic = new PanicMessage(digest, 0, clientId);
        panic.sign(clientId);

        replicaA.recvPrepare(prepare);

        // Prepare accepted
        Assert.isTrue(replicaA.getMessageLog().getTicket(viewNumber, seqNumber).getMessages().contains(prepare), "Prepare should be accpeted!");

        replicaA.recvPanic(panic);

        Assert.isTrue(replicaA.getMessageLog().getPanics().containsValue(panic), "Panic should have been accepted!");
    }

    @Test
	void testRecvNotExecutedPanic() {
        String clientId = "C0";
        RequestMessage request = new RequestMessage("123", 0, clientId);
        byte[] digest = replicaA.digest(request);
        PanicMessage panic = new PanicMessage(digest, 0, clientId);
        panic.sign(clientId);

        replicaA.recvPanic(panic);

        Assert.isTrue(!replicaA.getMessageLog().getPanics().containsValue(panic), "Panic should not have been accepted!");
    }

    @Test
	void testPrimaryRecvPanicFromClient() {
        HbftJavaReplica spyPrimary = Mockito.spy(primary);
        long viewNumber = 1;
        long seqNumber = 1;
        String clientId = "C0";
        RequestMessage request = new RequestMessage("123", 0, clientId);
        byte[] digest = spyPrimary.digest(request);
        PanicMessage panic = new PanicMessage(digest, 0, clientId);
        panic.sign(clientId);

        spyPrimary.recvRequest(request);

        // Prepare accepted
        Assert.isTrue(Arrays.equals(spyPrimary.getMessageLog().getTicket(viewNumber, seqNumber).getPrepare().getDigest(), digest), "Prepare should be accepted!");

        spyPrimary.recvPanic(panic);

        Assert.isTrue(spyPrimary.getMessageLog().getPanics().containsValue(panic), "Panic should have been accepted!");

        // PANIC should be forwarded
        verify(spyPrimary, times(1)).forwardPanic(panic);
        
        // It needs to send a checkpoint message as it is primary and got PANIC from client
        verify(spyPrimary, times(1)).broadcastMessageIncludingSelf(Mockito.any(CheckpointIMessage.class));
    }

    @Test
	void testPrimaryRecvPanicFromEnoughReplicas() {
        HbftJavaReplica spyPrimary = Mockito.spy(primary);
        long viewNumber = 1;
        long seqNumber = 1;
        String clientId = "C0";
        RequestMessage request = new RequestMessage("123", 0, clientId);
        byte[] digest = spyPrimary.digest(request);
        PanicMessage panic = new PanicMessage(digest, 0, clientId);

        spyPrimary.recvRequest(request);

        // Prepare accepted
        Assert.isTrue(Arrays.equals(spyPrimary.getMessageLog().getTicket(viewNumber, seqNumber).getPrepare().getDigest(), digest), "Prepare should be accepted!");

        panic.sign(replicaA.getId());
        spyPrimary.recvPanic(panic);
        panic.sign(replicaC.getId());
        spyPrimary.recvPanic(panic);
        

        Assert.isTrue(spyPrimary.getMessageLog().getPanics().containsValue(panic), "Panic should have been accepted!");

        // Replica got f+1 PANICs
        Assert.isTrue(spyPrimary.getMessageLog().checkPanicsForTimeout(1), "Received f+1 but timeout should not start as this is the primary!");
        verify(spyPrimary, times(0)).setTimeout(Mockito.any(Runnable.class), anyLong(), eq("PANIC"));

        // It should not send checkpoint yet as it did not receive 2f+1 panics
        verify(spyPrimary, times(0)).broadcastMessageIncludingSelf(Mockito.any(CheckpointIMessage.class));

        panic.sign(replicaD.getId());
        spyPrimary.recvPanic(panic);
        
        // It needs to send a checkpoint message as it is primary and got 2f+1 PANIC from replicas
        verify(spyPrimary, times(1)).broadcastMessageIncludingSelf(Mockito.any(CheckpointIMessage.class));
    }

    @Test
	void testRecvPanicAndStartTimer() {
        HbftJavaReplica spyReplica = Mockito.spy(replicaA);
        long viewNumber = 1;
        long seqNumber = 1;
        String clientId = "C0";
        RequestMessage request = new RequestMessage("123", 0, clientId);
        byte[] digest = spyReplica.digest(request);
        PrepareMessage prepare = new PrepareMessage(viewNumber, seqNumber, digest, request);
        PanicMessage panic = new PanicMessage(digest, 0, clientId);

        spyReplica.recvPrepare(prepare);

        // Prepare accepted
        Assert.isTrue(Arrays.equals(spyReplica.getMessageLog().getTicket(viewNumber, seqNumber).getPrepare().getDigest(), digest), "Prepare should be accepted!");

        panic.sign(replicaD.getId());
        spyReplica.recvPanic(panic);
        panic.sign(replicaC.getId());
        spyReplica.recvPanic(panic);
        
        // PANIC should be forwarded
        verify(spyReplica, times(2)).forwardPanic(Mockito.any(PanicMessage.class));

        Assert.isTrue(spyReplica.getMessageLog().getPanics().containsValue(panic), "Panic should have been accepted!");

        // Replica got f+1 PANICs
        Assert.isTrue(spyReplica.getMessageLog().checkPanicsForTimeout(1), "Received f+1 but timeout should not start as this is the primary!");
        verify(spyReplica, times(1)).setTimeout(Mockito.any(Runnable.class), anyLong(), eq("PANIC"));

        // It should not send checkpoint yet as it did not receive 2f+1 panics
        verify(spyReplica, times(0)).broadcastMessageIncludingSelf(Mockito.any(CheckpointIMessage.class));

    }

    @Test
	void tectRecvCheckpointINotFromPrimary() {
        HbftJavaReplica spyReplica = Mockito.spy(replicaA);
        long viewNumber = 1;
        long seqNumber = 1;
        String clientId = "C0";
        CheckpointMessage checkpoint = new CheckpointIMessage(0, null, replicaC.getId(), null);

        spyReplica.recvCheckpoint(checkpoint);
        
        //verify(spyReplica, times(0)).getMessageLog().appendCheckpoint(checkpoint, 1, null, viewNumber);
        verify(spyReplica, times(1)).sendViewChange(Mockito.any(ViewChangeMessage.class));
    }

    @Test
	void tectRecvCorrectCheckpointI() {
        HbftJavaReplica spyReplica = Mockito.spy(replicaA);
        long viewNumber = 1;
        long seqNumber = 1;
        String clientId = "C0";
        byte[] digest = spyReplica.digest(spyReplica.getSpeculativeHistory());
        CheckpointMessage checkpoint = new CheckpointIMessage(0, digest, primary.getId(), null);

        spyReplica.recvCheckpoint(checkpoint);
        
        verify(spyReplica, times(2)).clearTimeout(Mockito.any(String.class));
        verify(spyReplica, times(1)).sendCheckpoint(Mockito.any(CheckpointIIMessage.class));
    }
}
