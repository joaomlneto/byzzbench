// package byzzbench.simulator.protocols.hbft;

// import java.util.ArrayList;
// import java.util.Arrays;
// import java.util.Collection;
// import java.util.SortedMap;
// import java.util.SortedSet;
// import java.util.TreeMap;
// import java.util.TreeSet;

// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.mockito.Mockito;
// import static org.mockito.Mockito.times;
// import static org.mockito.Mockito.verify;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.util.Assert;

// import byzzbench.simulator.protocols.hbft.message.CheckpointIIIMessage;
// import byzzbench.simulator.protocols.hbft.message.CheckpointIIMessage;
// import byzzbench.simulator.protocols.hbft.message.CheckpointIMessage;
// import byzzbench.simulator.protocols.hbft.message.CheckpointMessage;
// import byzzbench.simulator.protocols.hbft.message.CommitMessage;
// import byzzbench.simulator.protocols.hbft.message.NewViewMessage;
// import byzzbench.simulator.protocols.hbft.message.PanicMessage;
// import byzzbench.simulator.protocols.hbft.message.PrepareMessage;
// import byzzbench.simulator.protocols.hbft.message.RequestMessage;
// import byzzbench.simulator.protocols.hbft.message.ViewChangeMessage;
// import byzzbench.simulator.protocols.hbft.pojo.ReplicaRequestKey;
// import byzzbench.simulator.protocols.hbft.pojo.ReplicaTicketPhase;
// import byzzbench.simulator.protocols.hbft.utils.Checkpoint;
// import byzzbench.simulator.protocols.hbft.utils.ScheduleLogger;
// import byzzbench.simulator.scheduler.Scheduler;
// import byzzbench.simulator.state.LogEntry;
// import byzzbench.simulator.state.SerializableLogEntry;

// @SpringBootTest
// public class HbftJavaReplicaTests {

//     private HbftJavaReplica replicaA;
//     private HbftJavaReplica replicaC;
//     private HbftJavaReplica replicaD;
//     private HbftJavaReplica primary;
//     private int tolerance = 1;
//     Scheduler scheduler = Mockito.mock(Scheduler.class);
//     ScheduleLogger logger = new ScheduleLogger();

//     @Test
//     void test() {}

//     @BeforeEach
//     void setup() {
//         String replicaAId = "A";
//         String primaryId = "B";
//         String replicaCId = "C";
//         String replicaDId = "D";
//         SortedSet<String> nodeIds = new TreeSet<>();
//         nodeIds.add("A");
//         nodeIds.add("B");
//         nodeIds.add("C");
//         nodeIds.add("D");
//         MessageLog messageLogA = new MessageLog(100, 100, 200);
//         MessageLog messageLogPrimary = new MessageLog(100, 100, 200);
//         MessageLog messageLogC = new MessageLog(100, 100, 200);
//         MessageLog messageLogD = new MessageLog(100, 100, 200);
        
//         /* 
//          * We need to have a Scheduler in order to be 
//          * able to test message reception and sending
//          */
//         HbftJavaScenario hbftScenario = new HbftJavaScenario(scheduler);
//         replicaA = new HbftJavaReplica<String, String>(replicaAId, nodeIds, tolerance, 5, messageLogA, hbftScenario);
//         replicaC = new HbftJavaReplica<String, String>(replicaCId, nodeIds, tolerance, 5, messageLogC, hbftScenario);
//         replicaD = new HbftJavaReplica<String, String>(replicaDId, nodeIds, tolerance, 5, messageLogD, hbftScenario);
//         primary = new HbftJavaReplica<String, String>(primaryId, nodeIds, tolerance, 5, messageLogPrimary, hbftScenario);

//         // Sets the viewNumber to 1 for the replicaA
//         replicaA.initialize();
//         replicaC.initialize();
//         primary.initialize();
//         logger.initialize(false);
//     }

//     @Test
// 	void testRecvDoubleRequestWithinView() {
//         HbftJavaReplica spyReplica = Mockito.spy(replicaA);
//         RequestMessage request = new RequestMessage("123", 0, "C0");
        
//         spyReplica.recvRequest(request);
//         spyReplica.recvRequest(request);
//         verify(spyReplica, times(1)).sendRequest(primary.getId(), request);
//         Assert.isTrue(replicaA.getReceivedRequests().get(request.getTimestamp()) != null, "Request should already be received");
//     }

//     // @Test
// 	// void testRecvDoubleRequestAccrossViews() {
//     //     HbftJavaReplica spyReplica = Mockito.spy(replicaA);
//     //     RequestMessage request = new RequestMessage("123", 0, "C0");
        
//     //     spyReplica.recvRequest(request);
//     //     spyReplica.enterNewView(2);
//     //     spyReplica.setCheckpointForNewView(false);
//     //     Assert.isTrue(replicaA.getReceivedRequests().get(request.getTimestamp()) == null, "Request should not be stored!");
//     //     spyReplica.recvRequest(request);
//     //     verify(spyReplica, times(1)).sendRequest(primary.getId(), request);
//     //     verify(spyReplica, times(1)).sendRequest(replicaC.getId(), request);
//     //     Assert.isTrue(replicaA.getReceivedRequests().get(request.getTimestamp()) != null, "Request should be stored!");
//     // }


//     @Test
// 	void testRecvPrepare() {
//         long viewNumber = 1;
//         long seqNumber = 1;
//         RequestMessage request = new RequestMessage("123", 0, "C0");
//         byte[] digest = replicaA.digest(request);
//         PrepareMessage prepare = new PrepareMessage(viewNumber, seqNumber, digest, request);
        
//         replicaA.recvPrepare(prepare);

//         // Prepare message should be accepted
//         Assert.isTrue(replicaA.getMessageLog().getTicket(viewNumber, seqNumber).getMessages().contains(prepare), "Prepare message with correct seqNum and viewNum should be accepted!");
//     }

//     @Test
// 	void testRecvWrongSeqPrepares() {
//         long viewNumber = 1;
//         long seqNumber = 2;
//         RequestMessage request = new RequestMessage("123", 0, "C0");
//         byte[] digest = replicaA.digest(request);
//         PrepareMessage prepare = new PrepareMessage(viewNumber, seqNumber, digest, request);
//         PrepareMessage prepare0 = new PrepareMessage(viewNumber, 0, digest, request);
        
//         replicaA.recvPrepare(prepare);
//         replicaA.recvPrepare(prepare0);

//         // Prepare message should not be accepted so ticket should be null
//         Assert.isTrue(replicaA.getMessageLog().getTicket(viewNumber, seqNumber) == null, "Prepare messages with incorrect seqNum not accepted!");
//         Assert.isTrue(replicaA.getMessageLog().getTicket(viewNumber, 0) == null, "Prepare messages with incorrect seqNum not accepted!");
//     }

//     @Test
// 	void testRecvWrongViewPrepares() {
//         long viewNumber = 2;
//         long seqNumber = 1;
//         RequestMessage request = new RequestMessage("123", 0, "C0");
//         byte[] digest = replicaA.digest(request);
//         PrepareMessage prepare = new PrepareMessage(viewNumber, seqNumber, digest, request);
//         PrepareMessage prepare0 = new PrepareMessage(0, seqNumber, digest, request);
        
//         replicaA.recvPrepare(prepare);
//         replicaA.recvPrepare(prepare0);

//         // Prepare message should not be accepted so ticket should be null
//         Assert.isTrue(replicaA.getMessageLog().getTicket(viewNumber, seqNumber) == null, "Prepare messages with incorrect viewNum not accepted!");
//         Assert.isTrue(replicaA.getMessageLog().getTicket(0, seqNumber) == null, "Prepare messages with incorrect viewNum not accepted!");
//     }
    
//     @Test
// 	void testRecvWrongDigestPrepare() {
//         long viewNumber = 1;
//         long seqNumber = 1;
//         RequestMessage request = new RequestMessage("123", 0, "C0");
//         RequestMessage request2 = new RequestMessage("321", 100, "C0");
//         byte[] digest = replicaA.digest(request2);
//         PrepareMessage prepare = new PrepareMessage(viewNumber, seqNumber, digest, request);
        
//         replicaA.recvPrepare(prepare);

//         // Prepare message should not be accepted
//         Assert.isTrue(replicaA.getMessageLog().getTicket(viewNumber, seqNumber) == null, "Prepare with incorrect digest should not be accepted!");
//     }

//     /* @Test
// 	void testRecvDiffSeqWithAlreadyAcceptedPrepare() {
//         long viewNumber = 1;
//         long seqNumber = 1;
//         RequestMessage request = new RequestMessage("123", 0, "C0");
//         byte[] digest = replicaA.digest(request);
//         PrepareMessage prepare = new PrepareMessage(viewNumber, seqNumber, digest, request);
//         PrepareMessage prepare2 = new PrepareMessage(viewNumber, seqNumber + 1, digest, request);
        
//         replicaA.recvPrepare(prepare);

//         // Correct prepare should be accepted
//         Assert.isTrue(replicaA.getMessageLog().getTicket(viewNumber, seqNumber).getMessages().contains(prepare), "Accept first prepare!");

//         replicaA.recvPrepare(prepare2);

//         // Prepare message should not be accepted
//         Assert.isTrue(replicaA.getMessageLog().getTicket(viewNumber, seqNumber + 1) == null, "Correct prepare with already ");
//     } */

//     @Test
// 	void testRecvCommit() {
//         long viewNumber = 1;
//         long seqNumber = 1;
//         RequestMessage request = new RequestMessage("123", 0, "C0");
//         byte[] digest = replicaA.digest(request);
//         //PrepareMessage prepare = new PrepareMessage(viewNumber, seqNumber, digest, request);
//         CommitMessage commit = new CommitMessage(viewNumber, seqNumber, digest, request, "B", this.replicaA.getSpeculativeHistory());

//         replicaA.recvCommit(commit);

//         // Commit message should be accepted
//         Assert.isTrue(replicaA.getMessageLog().getTicket(viewNumber, seqNumber).getMessages().contains(commit), "Commit message with correct seqNum and viewNum should be accepted!");
//         Assert.isTrue(!replicaA.getMessageLog().getTicket(viewNumber, seqNumber).isCommittedLocal(tolerance), "This commit should not accept local commit!");
//         Assert.isTrue(replicaA.getMessageLog().getTicket(viewNumber, seqNumber).getPhase() == ReplicaTicketPhase.PREPARE, "replicaA should still be in prepare phase!");

//         CommitMessage commit2 = new CommitMessage(viewNumber, seqNumber, digest, request, "C", this.replicaA.getSpeculativeHistory());
//         replicaA.recvCommit(commit2);

//         ReplicaRequestKey key = new ReplicaRequestKey("C0", 0);
//         Assert.isTrue(replicaA.getMessageLog().getTicketFromCache(key).isPrepared(tolerance), "Should have received f + 1 commits!");
//         Assert.isTrue(replicaA.getMessageLog().getTicketFromCache(key).isCommittedLocal(tolerance), "Should send commit and thus have 2f + 1 commits!");
//     }

//     @Test
// 	void testRecvCommitAfterPrepare() {
//         long viewNumber = 1;
//         long seqNumber = 1;
//         RequestMessage request = new RequestMessage("123", 0, "C0");
//         byte[] digest = replicaA.digest(request);
//         PrepareMessage prepare = new PrepareMessage(viewNumber, seqNumber, digest, request);
//         CommitMessage commit = new CommitMessage(viewNumber, seqNumber, digest, request, "B", this.replicaA.getSpeculativeHistory());

//         replicaA.recvPrepare(prepare);
//         replicaA.recvCommit(commit);

//         // Commit message should be accepted
//         Assert.isTrue(!replicaA.getMessageLog().getTicket(viewNumber, seqNumber).isCommittedLocal(tolerance), "This commit should not accept local commit!");
//         Assert.isTrue(replicaA.getMessageLog().getTicket(viewNumber, seqNumber).getPhase() == ReplicaTicketPhase.COMMIT, "replicaA should be in commit phase!");

//         CommitMessage commit2 = new CommitMessage(viewNumber, seqNumber, digest, request, "B", this.replicaA.getSpeculativeHistory());
//         replicaA.recvCommit(commit2);

//         ReplicaRequestKey key = new ReplicaRequestKey("C0", 0);
//         Assert.isTrue(replicaA.getMessageLog().getTicketFromCache(key).isPrepared(tolerance), "Should have received f + 1 commits!");
//         Assert.isTrue(replicaA.getMessageLog().getTicketFromCache(key).isCommittedLocal(tolerance), "Should send commit and thus have 2f + 1 commits!");
//     }

//     @Test
// 	void testRecvGoodPanic() {
//         long viewNumber = 1;
//         long seqNumber = 1;
//         String clientId = "C0";
//         RequestMessage request = new RequestMessage("123", 0, clientId);
//         byte[] digest = replicaA.digest(request);
//         PrepareMessage prepare = new PrepareMessage(viewNumber, seqNumber, digest, request);
//         PanicMessage panic = new PanicMessage(digest, 0, clientId);
//         panic.sign(clientId);

//         replicaA.recvPrepare(prepare);

//         // Prepare accepted
//         Assert.isTrue(replicaA.getMessageLog().getTicket(viewNumber, seqNumber).getMessages().contains(prepare), "Prepare should be accpeted!");

//         replicaA.recvPanic(panic);

//         Assert.isTrue(replicaA.getMessageLog().getPanics().containsValue(panic), "Panic should have been accepted!");
//     }

//     @Test
// 	void testRecvNotExecutedPanic() {
//         String clientId = "C0";
//         RequestMessage request = new RequestMessage("123", 0, clientId);
//         byte[] digest = replicaA.digest(request);
//         PanicMessage panic = new PanicMessage(digest, 0, clientId);
//         panic.sign(clientId);

//         replicaA.recvPanic(panic);

//         Assert.isTrue(!replicaA.getMessageLog().getPanics().containsValue(panic), "Panic should not have been accepted!");
//     }

//     /* @Test
// 	void testPrimaryRecvPanicFromClient() {
//         HbftJavaReplica spyPrimary = Mockito.spy(primary);
//         long viewNumber = 1;
//         long seqNumber = 1;
//         String clientId = "C0";
//         RequestMessage request = new RequestMessage("123", 0, clientId);
//         byte[] digest = spyPrimary.digest(request);
//         PanicMessage panic = new PanicMessage(digest, 0, clientId);
//         panic.sign(clientId);

//         spyPrimary.recvRequest(request);

//         // Prepare accepted
//         Assert.isTrue(Arrays.equals(spyPrimary.getMessageLog().getTicket(viewNumber, seqNumber).getPrepare().getDigest(), digest), "Prepare should be accepted!");

//         spyPrimary.recvPanic(panic);

//         Assert.isTrue(spyPrimary.getMessageLog().getPanics().containsValue(panic), "Panic should have been accepted!");

//         // PANIC should be forwarded
//         verify(spyPrimary, times(1)).forwardPanic(panic);
        
//         // It needs to send a checkpoint message as it is primary and got PANIC from client
//         verify(spyPrimary, times(1)).broadcastMessageIncludingSelf(Mockito.any(CheckpointIMessage.class));
//     } */
// /* 
//     @Test
// 	void testPrimaryRecvPanicFromEnoughReplicas() {
//         HbftJavaReplica spyPrimary = Mockito.spy(primary);
//         long viewNumber = 1;
//         long seqNumber = 1;
//         String clientId = "C0";
//         RequestMessage request = new RequestMessage("123", 0, clientId);
//         byte[] digest = spyPrimary.digest(request);
//         PanicMessage panic = new PanicMessage(digest, 0, clientId);

//         spyPrimary.recvRequest(request);

//         // Prepare accepted
//         Assert.isTrue(Arrays.equals(spyPrimary.getMessageLog().getTicket(viewNumber, seqNumber).getPrepare().getDigest(), digest), "Prepare should be accepted!");

//         panic.sign(replicaA.getId());
//         spyPrimary.recvPanic(panic);
//         panic.sign(replicaC.getId());
//         spyPrimary.recvPanic(panic);
        

//         Assert.isTrue(spyPrimary.getMessageLog().getPanics().containsValue(panic), "Panic should have been accepted!");

//         // Replica got f+1 PANICs
//         Assert.isTrue(spyPrimary.getMessageLog().checkPanicsForTimeout(1), "Received f+1 but timeout should not start as this is the primary!");
//         verify(spyPrimary, times(0)).setTimeout(Mockito.any(Runnable.class), anyLong(), eq("PANIC"));

//         // It should not send checkpoint yet as it did not receive 2f+1 panics
//         verify(spyPrimary, times(0)).broadcastMessageIncludingSelf(Mockito.any(CheckpointIMessage.class));

//         panic.sign(replicaD.getId());
//         spyPrimary.recvPanic(panic);
        
//         // It needs to send a checkpoint message as it is primary and got 2f+1 PANIC from replicas
//         verify(spyPrimary, times(1)).broadcastMessageIncludingSelf(Mockito.any(CheckpointIMessage.class));
//     }

//     @Test
// 	void testRecvPanicAndStartTimer() {
//         HbftJavaReplica spyReplica = Mockito.spy(replicaA);
//         long viewNumber = 1;
//         long seqNumber = 1;
//         String clientId = "C0";
//         RequestMessage request = new RequestMessage("123", 0, clientId);
//         byte[] digest = spyReplica.digest(request);
//         PrepareMessage prepare = new PrepareMessage(viewNumber, seqNumber, digest, request);
//         PanicMessage panic = new PanicMessage(digest, 0, clientId);

//         spyReplica.recvPrepare(prepare);

//         // Prepare accepted
//         Assert.isTrue(Arrays.equals(spyReplica.getMessageLog().getTicket(viewNumber, seqNumber).getPrepare().getDigest(), digest), "Prepare should be accepted!");

//         panic.sign(replicaD.getId());
//         spyReplica.recvPanic(panic);
//         panic.sign(replicaC.getId());
//         spyReplica.recvPanic(panic);
        
//         // PANIC should be forwarded
//         verify(spyReplica, times(2)).forwardPanic(Mockito.any(PanicMessage.class));

//         Assert.isTrue(spyReplica.getMessageLog().getPanics().containsValue(panic), "Panic should have been accepted!");

//         // Replica got f+1 PANICs
//         Assert.isTrue(spyReplica.getMessageLog().checkPanicsForTimeout(1), "Received f+1 but timeout should not start as this is the primary!");
//         verify(spyReplica, times(1)).setTimeout(Mockito.any(Runnable.class), anyLong(), eq("PANIC"));

//         // It should not send checkpoint yet as it did not receive 2f+1 panics
//         verify(spyReplica, times(0)).broadcastMessageIncludingSelf(Mockito.any(CheckpointIMessage.class));

//     }
//  */
//     @Test
// 	void testRecvIncorrectCheckpointI() {
//         HbftJavaReplica spyReplica = Mockito.spy(replicaA);
//         byte[] digest = spyReplica.digest(spyReplica.getSpeculativeHistory());
//         // Not from pirmary
//         CheckpointMessage checkpoint = new CheckpointIMessage(0, digest, replicaC.getId(), null);
//         // Wrong seq number
//         CheckpointMessage checkpoint2 = new CheckpointIMessage(1, digest, primary.getId(), null);
//         // Wrong speculativehistory digest
//         CheckpointMessage checkpoint3 = new CheckpointIMessage(0, null, primary.getId(), null);

//         spyReplica.recvCheckpoint(checkpoint);
//         spyReplica.recvCheckpoint(checkpoint2);
//         spyReplica.recvCheckpoint(checkpoint3);
        
//         //verify(spyReplica, times(0)).getMessageLog().appendCheckpoint(checkpoint, 1, null, viewNumber);
//         verify(spyReplica, times(3)).sendViewChange(Mockito.any(ViewChangeMessage.class));
//     }

//     @Test
// 	void testRecvCorrectCheckpointI() {
//         HbftJavaReplica spyReplica = Mockito.spy(replicaA);
//         byte[] digest = spyReplica.digest(spyReplica.getSpeculativeHistory());
//         CheckpointMessage checkpoint = new CheckpointIMessage(0, digest, primary.getId(), null);

//         spyReplica.recvCheckpoint(checkpoint);
        
//         verify(spyReplica, times(2)).clearTimeout(Mockito.any(String.class));
//         verify(spyReplica, times(1)).sendCheckpoint(Mockito.any(CheckpointIIMessage.class));
//     }

//     @Test
// 	void testRecvEnoughCorrectCheckpointII() {
//         HbftJavaReplica spyReplica = Mockito.spy(replicaA);
//         byte[] digest = spyReplica.digest(spyReplica.getSpeculativeHistory());
//         CheckpointMessage checkpoint = new CheckpointIMessage(0, digest, primary.getId(), primary.getSpeculativeHistory());
//         CheckpointMessage checkpoint2 = new CheckpointIIMessage(0, digest, replicaC.getId(), replicaC.getSpeculativeHistory());
//         CheckpointMessage checkpoint3 = new CheckpointIIMessage(0, digest, replicaD.getId(), replicaD.getSpeculativeHistory());

//         spyReplica.recvCheckpoint(checkpoint);
//         spyReplica.recvCheckpoint(checkpoint2);

//         Assert.isTrue(!replicaA.getMessageLog().isCER1(checkpoint2, 1), "Cer1 should not be complete with this checkpoint!");

//         spyReplica.recvCheckpoint(checkpoint3);
        
//         verify(spyReplica, times(1)).sendCheckpoint(Mockito.any(CheckpointIIMessage.class));
//         verify(spyReplica, times(1)).sendCheckpoint(Mockito.any(CheckpointIIIMessage.class));
//         Assert.isTrue(replicaA.getMessageLog().isCER1(checkpoint3, 1), "Cer1 should be complete with this checkpoint!");
//     }

//     @Test
// 	void testRecvDuplicateCorrectCheckpointII() {
//         HbftJavaReplica spyReplica = Mockito.spy(replicaA);
//         byte[] digest = spyReplica.digest(spyReplica.getSpeculativeHistory());
//         CheckpointMessage checkpoint = new CheckpointIMessage(0, digest, primary.getId(), primary.getSpeculativeHistory());
//         CheckpointMessage checkpoint2 = new CheckpointIIMessage(0, digest, replicaC.getId(), replicaC.getSpeculativeHistory());

//         spyReplica.recvCheckpoint(checkpoint);
//         spyReplica.recvCheckpoint(checkpoint2);
//         spyReplica.recvCheckpoint(checkpoint2);
        
//         verify(spyReplica, times(1)).sendCheckpoint(Mockito.any(CheckpointIIMessage.class));
//         verify(spyReplica, times(0)).sendCheckpoint(Mockito.any(CheckpointIIIMessage.class));
//         Assert.isTrue(!replicaA.getMessageLog().isCER1(checkpoint2, 1), "Cer1 should not be complete with this checkpoint!");
//     }

//     @Test
// 	void testRecvDiffHistoryCheckpointII() {
//         HbftJavaReplica spyReplica = Mockito.spy(replicaA);
//         RequestMessage request = new RequestMessage("123", 0, "C0");
//         SpeculativeHistory history = new SpeculativeHistory();
//         history.addEntry(1, request);
//         byte[] digest = spyReplica.digest(history);
//         CheckpointMessage checkpoint = new CheckpointIMessage(1, digest, primary.getId(), history);
//         CheckpointMessage checkpoint2 = new CheckpointIIMessage(1, digest, replicaC.getId(), history);
//         CheckpointMessage checkpoint3 = new CheckpointIIMessage(1, digest, replicaD.getId(), history);
//         CheckpointMessage checkpoint4 = new CheckpointIIMessage(1, digest, primary.getId(), history);

//         spyReplica.recvCheckpoint(checkpoint);
//         spyReplica.recvCheckpoint(checkpoint2);
//         spyReplica.recvCheckpoint(checkpoint3);
//         spyReplica.recvCheckpoint(checkpoint4);
        
//         verify(spyReplica, times(1)).sendViewChange(Mockito.any(ViewChangeMessage.class));
//         verify(spyReplica, times(0)).sendCheckpoint(Mockito.any(CheckpointIIMessage.class));
//         verify(spyReplica, times(1)).sendCheckpoint(Mockito.any(CheckpointIIIMessage.class));
//         Assert.isTrue(replicaA.getMessageLog().isCER1(checkpoint4, 1), "Cer1 should be complete with this checkpoint!");
//         Assert.isTrue(Arrays.equals(replicaA.digest(replicaA.getSpeculativeHistory()), digest), "Replica should have adopted the speculative hisotry!");
//     }

//     @Test
// 	void testRecvDiffHistoryCheckpointIII() {
//         HbftJavaReplica spyReplica = Mockito.spy(replicaA);
//         RequestMessage request = new RequestMessage("123", 0, "C0");
//         SpeculativeHistory history = new SpeculativeHistory();
//         history.addEntry(1, request);
//         byte[] digest = spyReplica.digest(history);
//         CheckpointMessage checkpoint = new CheckpointIMessage(1, digest, primary.getId(), history);
//         CheckpointMessage checkpoint2 = new CheckpointIIIMessage(1, digest, replicaC.getId(), history);
//         CheckpointMessage checkpoint3 = new CheckpointIIIMessage(1, digest, replicaD.getId(), history);
//         CheckpointMessage checkpoint4 = new CheckpointIIIMessage(1, digest, primary.getId(), history);

//         spyReplica.recvCheckpoint(checkpoint);
//         spyReplica.recvCheckpoint(checkpoint2);
//         spyReplica.recvCheckpoint(checkpoint3);
//         spyReplica.recvCheckpoint(checkpoint4);
        
//         verify(spyReplica, times(1)).sendViewChange(Mockito.any(ViewChangeMessage.class));
//         verify(spyReplica, times(0)).sendCheckpoint(Mockito.any(CheckpointIIMessage.class));
//         verify(spyReplica, times(0)).sendCheckpoint(Mockito.any(CheckpointIIIMessage.class));
//         Assert.isTrue(replicaA.getMessageLog().isCER2(checkpoint4, 1), "Cer2 should be complete with this checkpoint!");
//         Assert.isTrue(Arrays.equals(replicaA.digest(replicaA.getSpeculativeHistory()), digest), "Replica should have adopted the speculative hisotry!");
//     }

//     @Test
// 	void testRecvCheckpointIIIBehind() {
//         HbftJavaReplica spyReplica = Mockito.spy(replicaA);
//         String operation1 = "123";
//         RequestMessage request = new RequestMessage(operation1, 0, "C0");
//         //this.compute(seqNumber, new SerializableLogEntry(operation));
//         SpeculativeHistory history = new SpeculativeHistory();
//         history.addEntry(1, request);
//         byte[] digest = spyReplica.digest(history);
//         CheckpointMessage checkpoint2 = new CheckpointIIIMessage(1, digest, replicaC.getId(), history);
//         CheckpointMessage checkpoint3 = new CheckpointIIIMessage(1, digest, replicaD.getId(), history);
//         CheckpointMessage checkpoint4 = new CheckpointIIIMessage(1, digest, primary.getId(), history);

//         spyReplica.recvCheckpoint(checkpoint2);
//         spyReplica.recvCheckpoint(checkpoint3);
//         spyReplica.recvCheckpoint(checkpoint4);

//         Assert.isTrue(Arrays.equals(replicaA.digest(replicaA.getSpeculativeHistory()), digest), "Replica should have adopted the speculative hisotry!");
//     }

//     @Test
// 	void testRecvCheckpointIIIAhead() {
//         HbftJavaReplica spyReplica = Mockito.spy(replicaA);
//         LogEntry log1 = new SerializableLogEntry("123");
//         LogEntry log2 = new SerializableLogEntry("234");
//         replicaA.compute(1, log1);
//         replicaA.compute(2, log2);
//         replicaA.getSpeculativeHistory().addEntry(1, new RequestMessage("123", 0, "C0"));
//         replicaA.getSpeculativeHistory().addEntry(2, new RequestMessage("234", 0, "C0"));
//         RequestMessage request = new RequestMessage("123", 0, "C0");
//         SpeculativeHistory history = new SpeculativeHistory();
//         history.addEntry(1, request);
//         byte[] digest = spyReplica.digest(history);
//         CheckpointMessage checkpoint2 = new CheckpointIIIMessage(1, digest, replicaC.getId(), history);
//         CheckpointMessage checkpoint3 = new CheckpointIIIMessage(1, digest, replicaD.getId(), history);
//         CheckpointMessage checkpoint4 = new CheckpointIIIMessage(1, digest, primary.getId(), history);

//         spyReplica.recvCheckpoint(checkpoint2);
//         spyReplica.recvCheckpoint(checkpoint3);
//         spyReplica.recvCheckpoint(checkpoint4);

//         Assert.isTrue(Arrays.equals(replicaA.digest(replicaA.getSpeculativeHistory().getHistoryBefore(1)), digest), "Replicas history should match till seq 1!");
//         Assert.isTrue(replicaA.getCommitLog().getLength() == 2, "Correct commit log length!");
//         Assert.isTrue(replicaA.getCommitLog().get(1L).equals(log1), "Correct log1!");
//         Assert.isTrue(replicaA.getCommitLog().get(2L).equals(log2), "Correct log2!");
//     }

//     @Test
// 	void testRecvCheckpointIIIMissing() {
//         HbftJavaReplica spyReplica = Mockito.spy(replicaA);
//         LogEntry log1 = new SerializableLogEntry("123");
//         LogEntry log2 = new SerializableLogEntry("234");
//         replicaA.compute(2, log2);
//         replicaA.getSpeculativeHistory().addEntry(2, new RequestMessage("234", 0, "C0"));
//         RequestMessage request = new RequestMessage("123", 0, "C0");
//         RequestMessage request2 = new RequestMessage("234", 0, "C0");
//         SpeculativeHistory history = new SpeculativeHistory();
//         history.addEntry(1, request);
//         history.addEntry(2, request2);
//         byte[] digest = spyReplica.digest(history);
//         CheckpointMessage checkpoint2 = new CheckpointIIIMessage(2, digest, replicaC.getId(), history);
//         CheckpointMessage checkpoint3 = new CheckpointIIIMessage(2, digest, replicaD.getId(), history);
//         CheckpointMessage checkpoint4 = new CheckpointIIIMessage(2, digest, primary.getId(), history);

//         spyReplica.recvCheckpoint(checkpoint2);
//         spyReplica.recvCheckpoint(checkpoint3);
//         spyReplica.recvCheckpoint(checkpoint4);

//         Assert.isTrue(Arrays.equals(replicaA.digest(replicaA.getSpeculativeHistory()), digest), "Replicas history should match!");
//         Assert.isTrue(replicaA.getCommitLog().getLength() == 2, "Correct commit log length!");
//         Assert.isTrue(replicaA.getCommitLog().get(1L).equals(log1), "Correct log1!");
//         Assert.isTrue(replicaA.getCommitLog().get(2L).equals(log2), "Correct log2!");
//     }

//     @Test
// 	void testRecvCheckpointIIIMissingAndAhead() {
//         HbftJavaReplica spyReplica = Mockito.spy(replicaA);
//         LogEntry log1 = new SerializableLogEntry("123");
//         LogEntry log2 = new SerializableLogEntry("234");
//         LogEntry log3 = new SerializableLogEntry("345");
//         replicaA.compute(2, log2);
//         replicaA.compute(3, log3);
//         replicaA.getSpeculativeHistory().addEntry(2, new RequestMessage("234", 0, "C0"));
//         replicaA.getSpeculativeHistory().addEntry(3, new RequestMessage("345", 0, "C0"));
//         RequestMessage request = new RequestMessage("123", 0, "C0");
//         RequestMessage request2 = new RequestMessage("234", 0, "C0");
//         SpeculativeHistory history = new SpeculativeHistory();
//         history.addEntry(1, request);
//         history.addEntry(2, request2);
//         byte[] digest = spyReplica.digest(history);
//         CheckpointMessage checkpoint2 = new CheckpointIIIMessage(2, digest, replicaC.getId(), history);
//         CheckpointMessage checkpoint3 = new CheckpointIIIMessage(2, digest, replicaD.getId(), history);
//         CheckpointMessage checkpoint4 = new CheckpointIIIMessage(2, digest, primary.getId(), history);

//         spyReplica.recvCheckpoint(checkpoint2);
//         spyReplica.recvCheckpoint(checkpoint3);
//         spyReplica.recvCheckpoint(checkpoint4);

//         Assert.isTrue(Arrays.equals(replicaA.digest(replicaA.getSpeculativeHistory().getHistoryBefore(2)), digest), "Replicas history should match till seq 2!");
//         Assert.isTrue(replicaA.getCommitLog().getLength() == 3, "Correct commit log length!");
//         Assert.isTrue(replicaA.getCommitLog().get(1L).equals(log1), "Correct log1!");
//         Assert.isTrue(replicaA.getCommitLog().get(2L).equals(log2), "Correct log2!");
//         Assert.isTrue(replicaA.getCommitLog().get(3L).equals(log3), "Correct log3!");
//     }

//     @Test
// 	void testRecvCheckpointIIIMissingAndBehind() {
//         HbftJavaReplica spyReplica = Mockito.spy(replicaA);
//         LogEntry log1 = new SerializableLogEntry("123");
//         LogEntry log2 = new SerializableLogEntry("234");
//         LogEntry log3 = new SerializableLogEntry("345");
//         replicaA.compute(2, log2);
//         replicaA.getSpeculativeHistory().addEntry(2, new RequestMessage("234", 0, "C0"));
//         RequestMessage request = new RequestMessage("123", 0, "C0");
//         RequestMessage request2 = new RequestMessage("234", 0, "C0");
//         RequestMessage request3 = new RequestMessage("345", 0, "C0");
//         SpeculativeHistory history = new SpeculativeHistory();
//         history.addEntry(1, request);
//         history.addEntry(2, request2);
//         history.addEntry(3, request3);
//         byte[] digest = spyReplica.digest(history);
//         CheckpointMessage checkpoint2 = new CheckpointIIIMessage(3, digest, replicaC.getId(), history);
//         CheckpointMessage checkpoint3 = new CheckpointIIIMessage(3, digest, replicaD.getId(), history);
//         CheckpointMessage checkpoint4 = new CheckpointIIIMessage(3, digest, primary.getId(), history);

//         spyReplica.recvCheckpoint(checkpoint2);
//         spyReplica.recvCheckpoint(checkpoint3);
//         spyReplica.recvCheckpoint(checkpoint4);

//         Assert.isTrue(Arrays.equals(replicaA.digest(replicaA.getSpeculativeHistory()), digest), "Replicas history should match!");
//         Assert.isTrue(replicaA.getCommitLog().getLength() == 3, "Correct commit log length!");
//         Assert.isTrue(replicaA.getCommitLog().get(1L).equals(log1), "Correct log1!");
//         Assert.isTrue(replicaA.getCommitLog().get(2L).equals(log2), "Correct log2!");
//         Assert.isTrue(replicaA.getCommitLog().get(3L).equals(log3), "Correct log3!");
//     }

//     @Test
// 	void testRecvIncorrectSeqNumCheckpointIII() {
//         HbftJavaReplica spyReplica = Mockito.spy(replicaA);
//         RequestMessage request = new RequestMessage("123", 0, "C0");
//         SpeculativeHistory history = new SpeculativeHistory();
//         //history.addEntry(1, request);
//         history.addEntry(1, request);
//         SpeculativeHistory history2 = new SpeculativeHistory();
//         history2.addEntry(0, request);
//         history2.addEntry(1, request);
//         byte[] digest = spyReplica.digest(history);
//         byte[] digest2 = spyReplica.digest(history2);
//         //CheckpointMessage checkpoint = new CheckpointIMessage(1, digest, primary.getId(), history);
//         CheckpointMessage checkpoint2 = new CheckpointIIIMessage(1, digest, replicaC.getId(), history);
//         CheckpointMessage checkpoint3 = new CheckpointIIIMessage(1, digest, replicaD.getId(), history);
//         CheckpointMessage checkpoint4 = new CheckpointIIIMessage(1, digest2, primary.getId(), history);

//         //spyReplica.recvCheckpoint(checkpoint);
//         spyReplica.recvCheckpoint(checkpoint2);
//         spyReplica.recvCheckpoint(checkpoint3);
//         spyReplica.recvCheckpoint(checkpoint4);
        
//         //verify(spyReplica, times(1)).sendViewChange(Mockito.any(ViewChangeMessage.class));
//         //verify(spyReplica, times(0)).sendCheckpoint(Mockito.any(CheckpointIIMessage.class));
//         verify(spyReplica, times(0)).sendCheckpoint(Mockito.any(CheckpointIIIMessage.class));
//         // Assert.isTrue(replicaA.getMessageLog().isCER2(checkpoint4, 1), "Cer2 should be complete with this checkpoint!");
//         // Assert.isTrue(Arrays.equals(replicaA.digest(replicaA.getSpeculativeHistory()), digest), "Replica should have adopted the speculative hisotry!");
//         verify(spyReplica, times(0)).adjustHistory(Mockito.any(SortedMap.class));
//     }

//     @Test
// 	void testRecvEnoughCorrectViewChange() {
//         HbftJavaReplica spyReplica = Mockito.spy(replicaA);
//         ViewChangeMessage viewChange  = new ViewChangeMessage(2, null, null, null, replicaC.getId());
//         ViewChangeMessage viewChange2  = new ViewChangeMessage(2, null, null, null, replicaD.getId());
//         //ViewChangeMessage viewChange  = new ViewChangeMessage(2, null, null, null, replicaC.getId());


//         spyReplica.recvViewChange(viewChange);
//         spyReplica.recvViewChange(viewChange);

//         // ViewChanges are sorted per replica so a double vote
//         // should not lead to two separate viewChange messages
//         verify(spyReplica, times(0)).sendViewChange(Mockito.any(ViewChangeMessage.class));
//         spyReplica.recvViewChange(viewChange2);
//         verify(spyReplica, times(1)).sendViewChange(Mockito.any(ViewChangeMessage.class));
//     }

//     // TODO: Figure out how viewChanges with different views should be handled
//     @Test
// 	void testRecvWithTwoDifferentViewsViewChange() {
//         HbftJavaReplica spyReplica = Mockito.spy(replicaA);
//         ViewChangeMessage viewChange  = new ViewChangeMessage(2, null, null, null, replicaC.getId());
//         ViewChangeMessage viewChange2  = new ViewChangeMessage(3, null, null, null, replicaD.getId());
//         //ViewChangeMessage viewChange  = new ViewChangeMessage(2, null, null, null, replicaC.getId());


//         spyReplica.recvViewChange(viewChange);
//         spyReplica.recvViewChange(viewChange);

//         // ViewChanges are sorted per replica so a double vote
//         // should not lead to two separate viewChange messages
//         verify(spyReplica, times(0)).sendViewChange(Mockito.any(ViewChangeMessage.class));
//         spyReplica.recvViewChange(viewChange2);
//         //verify(spyReplica, times(1)).sendViewChange(Mockito.any(ViewChangeMessage.class));
//     }

//     @Test
// 	void testRecvWithDifferentReqInViewChange() {
//         HbftJavaReplica spyReplica = Mockito.spy(replicaC);
//         RequestMessage request = new RequestMessage("123", 0, "C1");
//         RequestMessage request2 = new RequestMessage("321", 0, "C0");
//         SortedMap<Long, RequestMessage> rRequests1 = new TreeMap<>();
//         SortedMap<Long, RequestMessage> rRequests2 = new TreeMap<>();
//         rRequests1.put(1L, request);
//         rRequests2.put(1L, request2);
//         ViewChangeMessage viewChange  = new ViewChangeMessage(2, null, null, rRequests1, replicaA.getId());
//         ViewChangeMessage viewChange2  = new ViewChangeMessage(2, null, null, rRequests2, replicaD.getId());
//         SpeculativeHistory history = new SpeculativeHistory();
//         history.addEntry(1, null);
//         NewViewMessage newView = new NewViewMessage(2, null, null, history);

//         spyReplica.recvViewChange(viewChange);
//         spyReplica.recvViewChange(viewChange2);

//         verify(spyReplica, times(1)).sendViewChange(Mockito.any(ViewChangeMessage.class));
//         // Should not include any request in R        
//         verify(spyReplica, times(1))
//         .recvNewView(Mockito.argThat(arg -> arg.getNewViewNumber() == newView.getNewViewNumber()
//             && arg.getSpeculativeHistory().getRequests().size() == 1
//             && arg.getSpeculativeHistory().getRequests().get(1L) == null));
//     }

//     @Test
// 	void testRecvViewChangeAndTimeout() {
//         HbftJavaReplica spyReplica = Mockito.spy(replicaA);
//         ViewChangeMessage viewChange  = new ViewChangeMessage(2, null, null, null, replicaC.getId());
//         ViewChangeMessage viewChange2  = new ViewChangeMessage(2, null, null, null, replicaD.getId());

//         spyReplica.recvViewChange(viewChange);
//         spyReplica.recvViewChange(viewChange2);

//         verify(spyReplica, times(1)).sendViewChange(Mockito.any(ViewChangeMessage.class));
//         // Timeout for viewChange
//         spyReplica.incrementViewChangeOnTimeout();
//         verify(spyReplica, times(1)).sendViewChange(Mockito.argThat(arg -> arg.getNewViewNumber() == 3L));
//     }

//     @Test
// 	void testIfViewChangeIsWellConstructed() {
//         HbftJavaReplica spyReplica = Mockito.spy(replicaA);
//         RequestMessage request = new RequestMessage("123", 0, "C0");
//         byte[] digest = replicaA.digest(request);
//         long viewNumber = 1;
//         long seqNumber = 1;
//         PrepareMessage prepare = new PrepareMessage(viewNumber, seqNumber, digest, request);
//         CommitMessage commit = new CommitMessage(viewNumber, seqNumber, digest, request, primary.getId(), primary.getSpeculativeHistory());
//         CommitMessage commit2 = new CommitMessage(viewNumber, seqNumber, digest, request, replicaC.getId(), replicaC.getSpeculativeHistory());
        
//         spyReplica.recvPrepare(prepare);
//         spyReplica.recvCommit(commit);
//         spyReplica.recvCommit(commit2);

//         Assert.isTrue(replicaA.getSpeculativeHistory().getRequests().containsValue(request) && replicaA.getSpeculativeHistory().getRequests().containsKey(seqNumber), "Correct speculative history!");
        
//         CheckpointMessage checkpoint = new CheckpointIMessage(seqNumber, replicaA.digest(replicaA.getSpeculativeHistory()), primary.getId(), replicaA.getSpeculativeHistory());
//         CheckpointMessage checkpoint2 = new CheckpointIIMessage(seqNumber, replicaA.digest(replicaA.getSpeculativeHistory()), replicaC.getId(), replicaA.getSpeculativeHistory());
//         CheckpointMessage checkpoint3 = new CheckpointIIMessage(seqNumber, replicaA.digest(replicaA.getSpeculativeHistory()), replicaD.getId(), replicaA.getSpeculativeHistory());
//         CheckpointMessage checkpoint4 = new CheckpointIIIMessage(seqNumber, replicaA.digest(replicaA.getSpeculativeHistory()), replicaC.getId(), replicaA.getSpeculativeHistory());
//         CheckpointMessage checkpoint5 = new CheckpointIIIMessage(seqNumber, replicaA.digest(replicaA.getSpeculativeHistory()), replicaD.getId(), replicaA.getSpeculativeHistory());

//         spyReplica.recvCheckpoint(checkpoint);
//         spyReplica.recvCheckpoint(checkpoint2);
//         spyReplica.recvCheckpoint(checkpoint3);
//         spyReplica.recvCheckpoint(checkpoint4);
//         spyReplica.recvCheckpoint(checkpoint5);

//         Assert.isTrue(replicaA.getMessageLog().isCER1inView(viewNumber, 1) == replicaA.getSpeculativeHistory(), "Cer1 should be in view!");

//         RequestMessage request2 = new RequestMessage("321", 10, "C0");
//         byte[] digest2 = replicaA.digest(request2);
//         long seqNumber2 = 2;
//         PrepareMessage prepare2 = new PrepareMessage(viewNumber, seqNumber2, digest2, request2);

//         spyReplica.recvPrepare(prepare2);

//         Assert.isTrue(replicaA.getSpeculativeRequests().containsValue(request2), "Correct speculatively executed requests!");

//         spyReplica.sendViewChangeOnTimeout();

//         verify(spyReplica, times(1))
//         .sendViewChange(Mockito.argThat(arg -> arg.getNewViewNumber() == viewNumber + 1 
//             && arg.getRequestsR().values().size() == 1
//             && arg.getRequestsR().get(arg.getRequestsR().lastKey()) == replicaA.getSpeculativeRequests().get(replicaA.getSpeculativeRequests().lastKey())
//             && arg.getSpeculativeHistoryP() == checkpoint.getHistory()
//             && arg.getSpeculativeHistoryQ().getHistory() == checkpoint.getHistory()));
//     }

//     @Test
// 	void testIfViewChangeIsWellConstructed2() {
//         HbftJavaReplica spyReplica = Mockito.spy(replicaA);
//         RequestMessage request = new RequestMessage("123", 0, "C0");
//         byte[] digest = replicaA.digest(request);
//         long viewNumber = 1;
//         long seqNumber = 1;
//         PrepareMessage prepare = new PrepareMessage(viewNumber, seqNumber, digest, request);
//         CommitMessage commit = new CommitMessage(viewNumber, seqNumber, digest, request, primary.getId(), primary.getSpeculativeHistory());
//         CommitMessage commit2 = new CommitMessage(viewNumber, seqNumber, digest, request, replicaC.getId(), replicaC.getSpeculativeHistory());
        
//         spyReplica.recvPrepare(prepare);
//         spyReplica.recvCommit(commit);
//         spyReplica.recvCommit(commit2);

//         Assert.isTrue(replicaA.getSpeculativeHistory().getRequests().containsValue(request) && replicaA.getSpeculativeHistory().getRequests().containsKey(seqNumber), "Correct speculative history!");
        
//         CheckpointMessage checkpoint = new CheckpointIIMessage(seqNumber, replicaA.digest(replicaA.getSpeculativeHistory()), primary.getId(), replicaA.getSpeculativeHistory());
//         CheckpointMessage checkpoint2 = new CheckpointIIMessage(seqNumber, replicaA.digest(replicaA.getSpeculativeHistory()), replicaC.getId(), replicaA.getSpeculativeHistory());
//         CheckpointMessage checkpoint3 = new CheckpointIIMessage(seqNumber, replicaA.digest(replicaA.getSpeculativeHistory()), replicaD.getId(), replicaA.getSpeculativeHistory());
//         CheckpointMessage checkpoint4 = new CheckpointIIIMessage(seqNumber, replicaA.digest(replicaA.getSpeculativeHistory()), replicaC.getId(), replicaA.getSpeculativeHistory());
//         CheckpointMessage checkpoint5 = new CheckpointIIIMessage(seqNumber, replicaA.digest(replicaA.getSpeculativeHistory()), replicaD.getId(), replicaA.getSpeculativeHistory());

//         spyReplica.recvCheckpoint(checkpoint);
//         spyReplica.recvCheckpoint(checkpoint2);
//         spyReplica.recvCheckpoint(checkpoint3);
//         spyReplica.recvCheckpoint(checkpoint4);
//         spyReplica.recvCheckpoint(checkpoint5);

//         Assert.isTrue(replicaA.getMessageLog().isCER1inView(viewNumber, 1) == replicaA.getSpeculativeHistory(), "Cer1 should be in view!");

//         RequestMessage request2 = new RequestMessage("321", 10, "C0");
//         byte[] digest2 = replicaA.digest(request2);
//         long seqNumber2 = 2;
//         PrepareMessage prepare2 = new PrepareMessage(viewNumber, seqNumber2, digest2, request2);

//         spyReplica.recvPrepare(prepare2);

//         Assert.isTrue(replicaA.getSpeculativeRequests().containsValue(request2), "Correct speculatively executed requests!");

//         spyReplica.sendViewChangeOnTimeout();

//         verify(spyReplica, times(1))
//         .sendViewChange(Mockito.argThat(arg -> arg.getNewViewNumber() == viewNumber + 1 
//             && arg.getRequestsR().values().size() == 1
//             && arg.getRequestsR().get(arg.getRequestsR().lastKey()) == replicaA.getSpeculativeRequests().get(replicaA.getSpeculativeRequests().lastKey())
//             && arg.getSpeculativeHistoryP() == checkpoint.getHistory()
//             && arg.getSpeculativeHistoryQ() == null));
//     }

//     @Test
// 	void testIfViewChangeIsWellConstructed3() {
//         HbftJavaReplica spyReplica = Mockito.spy(replicaA);
//         RequestMessage request = new RequestMessage("123", 0, "C0");
//         byte[] digest = replicaA.digest(request);
//         long viewNumber = 1;
//         long seqNumber = 1;
//         PrepareMessage prepare = new PrepareMessage(viewNumber, seqNumber, digest, request);
//         CommitMessage commit = new CommitMessage(viewNumber, seqNumber, digest, request, primary.getId(), primary.getSpeculativeHistory());
//         CommitMessage commit2 = new CommitMessage(viewNumber, seqNumber, digest, request, replicaC.getId(), replicaC.getSpeculativeHistory());
        
//         spyReplica.recvPrepare(prepare);
//         spyReplica.recvCommit(commit);
//         spyReplica.recvCommit(commit2);

//         Assert.isTrue(replicaA.getSpeculativeHistory().getRequests().containsValue(request) && replicaA.getSpeculativeHistory().getRequests().containsKey(seqNumber), "Correct speculative history!");
        
//         CheckpointMessage checkpoint = new CheckpointIMessage(seqNumber, replicaA.digest(replicaA.getSpeculativeHistory()), primary.getId(), replicaA.getSpeculativeHistory());;

//         spyReplica.recvCheckpoint(checkpoint);

//         Assert.isTrue(replicaA.getMessageLog().isCER1inView(viewNumber, 1) == null, "Cer1 should be null!");

//         RequestMessage request2 = new RequestMessage("321", 10, "C0");
//         byte[] digest2 = replicaA.digest(request2);
//         long seqNumber2 = 2;
//         PrepareMessage prepare2 = new PrepareMessage(viewNumber, seqNumber2, digest2, request2);

//         spyReplica.recvPrepare(prepare2);

//         Assert.isTrue(replicaA.getSpeculativeRequests().containsValue(request) && replicaA.getSpeculativeRequests().containsValue(request2), "Correct speculatively executed requests!");

//         spyReplica.sendViewChangeOnTimeout();

//         verify(spyReplica, times(1))
//         .sendViewChange(Mockito.argThat(arg -> arg.getNewViewNumber() == viewNumber + 1 
//             && arg.getRequestsR().values().size() == 2
//             && arg.getRequestsR().get(arg.getRequestsR().firstKey()) == replicaA.getSpeculativeRequests().get(replicaA.getSpeculativeRequests().firstKey())
//             && arg.getRequestsR().get(arg.getRequestsR().lastKey()) == replicaA.getSpeculativeRequests().get(replicaA.getSpeculativeRequests().lastKey())
//             && arg.getSpeculativeHistoryP() == null
//             && arg.getSpeculativeHistoryQ().getHistory() == replicaA.getSpeculativeHistory()));
//     }

//     /* @Test
// 	void testLotOfDifferentViewChanges() {
//         HbftJavaReplica spyReplica = Mockito.spy(replicaC);
//         RequestMessage request = new RequestMessage("123", 0, "C0");
//         byte[] digest = replicaC.digest(request);
//         long viewNumber = 1;
//         long seqNumber = 1;
//         PrepareMessage prepare = new PrepareMessage(viewNumber, seqNumber, digest, request);
//         CommitMessage commit = new CommitMessage(viewNumber, seqNumber, digest, request, primary.getId(), primary.getSpeculativeHistory());
//         CommitMessage commit2 = new CommitMessage(viewNumber, seqNumber, digest, request, replicaD.getId(), replicaD.getSpeculativeHistory());
        
//         spyReplica.recvPrepare(prepare);
//         spyReplica.recvCommit(commit);
//         spyReplica.recvCommit(commit2);

//         ViewChangeMessage viewChange  = new ViewChangeMessage(2, null, null, new TreeMap<>(), replicaA.getId());
//         ViewChangeMessage viewChange2  = new ViewChangeMessage(3, null, null, new TreeMap<>(), replicaD.getId());
//         ViewChangeMessage viewChange3  = new ViewChangeMessage(4, null, null, new TreeMap<>(), replicaD.getId());
//         ViewChangeMessage viewChange4  = new ViewChangeMessage(5, null, null, new TreeMap<>(), replicaD.getId());
//         ViewChangeMessage viewChange5  = new ViewChangeMessage(2, null, null, new TreeMap<>(), replicaD.getId());

//         spyReplica.recvViewChange(viewChange);
//         spyReplica.recvViewChange(viewChange2);
//         spyReplica.recvViewChange(viewChange3);
//         spyReplica.recvViewChange(viewChange4);
//         spyReplica.recvViewChange(viewChange5);

//         verify(spyReplica, times(1))
//         .sendViewChange(Mockito.argThat(arg -> arg.getNewViewNumber() == viewNumber + 1 
//             && arg.getRequestsR().values().size() == 1
//             && arg.getRequestsR().get(arg.getRequestsR().firstKey()) == replicaC.getSpeculativeRequests().get(replicaC.getSpeculativeRequests().firstKey())
//             && arg.getSpeculativeHistoryP() == null
//             && arg.getSpeculativeHistoryQ() == null));
//         verify(spyReplica, times(1))
//         .recvNewView(Mockito.argThat(arg -> arg.getNewViewNumber() == viewNumber + 1 
//             && arg.getCheckpoint().getSequenceNumber() == 0
//             && arg.getCheckpoint().getHistory() == null
//             && arg.getSpeculativeHistory().getHistory().get(1L) == null
//             && arg.getViewChangeProofs().contains(viewChange)
//             && arg.getViewChangeProofs().contains(viewChange5)));
//     } */

//     /* 
//      * Duplicate view-changes from same replica with small change
//      */
//     @Test
// 	void testDuplicateViewChanges() {
//         HbftJavaReplica spyReplica = Mockito.spy(replicaA);
//         RequestMessage request = new RequestMessage("123", 0, "C0");
//         Checkpoint checkpoint = new Checkpoint(0, null);
//         long seqNumber = 1;
//         SpeculativeHistory history = new SpeculativeHistory();
//         history.addEntry(seqNumber, request);
//         ViewChangeMessage viewChange  = new ViewChangeMessage(2, null, null, null, replicaC.getId());
//         ViewChangeMessage viewChange2  = new ViewChangeMessage(2, null, null, history.getRequests(), replicaC.getId());
        
//         spyReplica.recvViewChange(viewChange);
//         spyReplica.recvViewChange(viewChange2);
        
//         Assert.isTrue(!replicaA.getMessageLog().getViewChanges().get(2L).get(replicaC.getId()).equals(viewChange), "Should not contain first view change!");
//         Assert.isTrue(replicaA.getMessageLog().getViewChanges().get(2L).get(replicaC.getId()).equals(viewChange2), "Should contain second view change!");
//     }

//     /* 
//      * Test1 - Correct with replica being empty, and receiving null checkpoint
//      * and 1 request in R with correct view-changes
//      */
//     @Test
// 	void testRecvCorrectNewView() {
//         HbftJavaReplica spyReplica = Mockito.spy(replicaA);
//         RequestMessage request = new RequestMessage("123", 0, "C0");
//         Checkpoint checkpoint = new Checkpoint(0, null);
//         long seqNumber = 1;
//         SpeculativeHistory history = new SpeculativeHistory();
//         history.addEntry(seqNumber, request);
//         ViewChangeMessage viewChange  = new ViewChangeMessage(2, null, null, history.getRequests(), replicaC.getId());
//         ViewChangeMessage viewChange2  = new ViewChangeMessage(2, null, null, history.getRequests(), replicaD.getId());
//         ViewChangeMessage viewChange3  = new ViewChangeMessage(2, null, null, history.getRequests(), replicaA.getId());
//         Collection<ViewChangeMessage> viewChanges = new ArrayList<>();
//         viewChanges.add(viewChange);
//         viewChanges.add(viewChange2);
//         viewChanges.add(viewChange3);
//         NewViewMessage newView = new NewViewMessage(2, viewChanges, checkpoint, history);
        
//         spyReplica.recvNewView(newView);
        
//         Assert.isTrue(replicaA.getMessageLog().acceptNewView(newView, 1, logger), "Should accept the new view!");

//         verify(spyReplica, times(1)).enterNewView(newView.getNewViewNumber());
//     }

//     /* 
//      * Test2 - Correct with replica being empty, and receiving null checkpoint
//      * and 1 request in R with correct view-changes
//      * 1 view change has an extra request
//      */
//     @Test
// 	void testRecvCorrectNewView2() {
//         HbftJavaReplica spyReplica = Mockito.spy(replicaA);
//         RequestMessage request = new RequestMessage("123", 0, "C0");
//         RequestMessage request2 = new RequestMessage("321", 0, "C0");
//         Checkpoint checkpoint = new Checkpoint(0, null);
//         long seqNumber = 1;
//         SpeculativeHistory history = new SpeculativeHistory();
//         history.addEntry(seqNumber, request);
//         SpeculativeHistory history2 = new SpeculativeHistory();
//         history2.addEntry(seqNumber, request);
//         history2.addEntry(seqNumber + 1, request2);
//         ViewChangeMessage viewChange  = new ViewChangeMessage(2, null, null, history.getRequests(), replicaC.getId());
//         ViewChangeMessage viewChange2  = new ViewChangeMessage(2, null, null, history2.getRequests(), replicaD.getId());
//         ViewChangeMessage viewChange3  = new ViewChangeMessage(2, null, null, history.getRequests(), replicaD.getId());
//         Collection<ViewChangeMessage> viewChanges = new ArrayList<>();
//         viewChanges.add(viewChange);
//         viewChanges.add(viewChange2);
//         viewChanges.add(viewChange3);
//         SpeculativeHistory historyNewView = new SpeculativeHistory();
//         historyNewView.addEntry(seqNumber, request);
//         historyNewView.addEntry(seqNumber + 1, null);
//         NewViewMessage newView = new NewViewMessage(2, viewChanges, checkpoint, historyNewView);
        
//         spyReplica.recvNewView(newView);
        
//         Assert.isTrue(replicaA.getMessageLog().acceptNewView(newView, 1, logger), "Should accept the new view!");

//         verify(spyReplica, times(1)).enterNewView(newView.getNewViewNumber());
//     }

//     /* 
//      * Test3 - Correct with correct Q and P
//      */
//     @Test
// 	void testRecvCorrectNewView3() {
//         HbftJavaReplica spyReplica = Mockito.spy(replicaA);
//         RequestMessage request = new RequestMessage("123", 0, "C0");
//         RequestMessage request2 = new RequestMessage("321", 0, "C0");
//         long seqNumber = 1;
//         SpeculativeHistory history = new SpeculativeHistory();
//         history.addEntry(seqNumber, request);
//         SpeculativeHistory history2 = new SpeculativeHistory();
//         history2.addEntry(seqNumber, request);
//         history2.addEntry(seqNumber + 1, request2);
//         ViewChangeMessage viewChange  = new ViewChangeMessage(2, history, new Checkpoint(history.getGreatestSeqNumber(), history), history.getRequests(), replicaC.getId());
//         ViewChangeMessage viewChange2  = new ViewChangeMessage(2, history, null, history2.getRequests(), primary.getId());
//         ViewChangeMessage viewChange3  = new ViewChangeMessage(2, history, new Checkpoint(history.getGreatestSeqNumber(), history), history.getRequests(), replicaD.getId());
//         Collection<ViewChangeMessage> viewChanges = new ArrayList<>();
//         viewChanges.add(viewChange);
//         viewChanges.add(viewChange2);
//         viewChanges.add(viewChange3);
//         SpeculativeHistory historyNewView = new SpeculativeHistory();
//         historyNewView.addEntry(seqNumber, request);
//         historyNewView.addEntry(seqNumber + 1, null);
//         NewViewMessage newView = new NewViewMessage(2, viewChanges, new Checkpoint(history.getGreatestSeqNumber(), history), historyNewView);
        
//         spyReplica.recvNewView(newView);
        
//         Assert.isTrue(replicaA.getMessageLog().acceptNewView(newView, 1, logger), "Should accept the new view!");

//         verify(spyReplica, times(1)).enterNewView(newView.getNewViewNumber());
//     }

//     /* 
//      * IncorrectTest1 - Incorrect with replica being empty,
//      * receiving requests which the view changes dont support
//      */
//     @Test
// 	void testRecvIncorrectNewView() {
//         HbftJavaReplica spyReplica = Mockito.spy(replicaA);
//         RequestMessage request = new RequestMessage("123", 0, "C0");
//         RequestMessage request2 = new RequestMessage("321", 0, "C0");
//         Checkpoint checkpoint = new Checkpoint(0, null);
//         long seqNumber = 1;
//         SpeculativeHistory history = new SpeculativeHistory();
//         history.addEntry(seqNumber, request);
//         SpeculativeHistory history2 = new SpeculativeHistory();
//         history2.addEntry(seqNumber, request);
//         history2.addEntry(seqNumber + 1, request2);
//         ViewChangeMessage viewChange  = new ViewChangeMessage(2, null, null, history.getRequests(), replicaC.getId());
//         ViewChangeMessage viewChange2  = new ViewChangeMessage(2, null, null, history2.getRequests(), replicaD.getId());
//         ViewChangeMessage viewChange3  = new ViewChangeMessage(2, null, null, history.getRequests(), replicaD.getId());
//         Collection<ViewChangeMessage> viewChanges = new ArrayList<>();
//         viewChanges.add(viewChange);
//         viewChanges.add(viewChange2);
//         viewChanges.add(viewChange3);
//         SpeculativeHistory historyNewView = new SpeculativeHistory();
//         historyNewView.addEntry(seqNumber, request);
//         historyNewView.addEntry(seqNumber + 1, request2);
//         NewViewMessage newView = new NewViewMessage(2, viewChanges, checkpoint, historyNewView);
        
//         spyReplica.recvNewView(newView);
        
//         Assert.isTrue(!replicaA.getMessageLog().acceptNewView(newView, 1, logger), "Should not accept the new view!");

//         verify(spyReplica, times(0)).enterNewView(newView.getNewViewNumber());
//     }

//     /* 
//      * IncorrectTest2 - Incorrect niew-view number
//      */
//     @Test
// 	void testRecvIncorrectNewView2() {
//         HbftJavaReplica spyReplica = Mockito.spy(replicaA);
//         RequestMessage request = new RequestMessage("123", 0, "C0");
//         RequestMessage request2 = new RequestMessage("321", 0, "C0");
//         Checkpoint checkpoint = new Checkpoint(0, null);
//         long seqNumber = 1;
//         SpeculativeHistory history = new SpeculativeHistory();
//         history.addEntry(seqNumber, request);
//         SpeculativeHistory history2 = new SpeculativeHistory();
//         history2.addEntry(seqNumber, request);
//         history2.addEntry(seqNumber + 1, request2);
//         ViewChangeMessage viewChange  = new ViewChangeMessage(2, null, null, history.getRequests(), replicaC.getId());
//         ViewChangeMessage viewChange2  = new ViewChangeMessage(3, null, null, history2.getRequests(), replicaD.getId());
//         ViewChangeMessage viewChange3  = new ViewChangeMessage(2, null, null, history.getRequests(), replicaD.getId());
//         Collection<ViewChangeMessage> viewChanges = new ArrayList<>();
//         viewChanges.add(viewChange);
//         viewChanges.add(viewChange2);
//         viewChanges.add(viewChange3);
        
//         NewViewMessage newView = new NewViewMessage(2, viewChanges, checkpoint, history);
    
//         spyReplica.recvNewView(newView);
//         Assert.isTrue(!replicaA.getMessageLog().acceptNewView(newView, 1, logger), "Should not accept the new view!");
//         verify(spyReplica, times(0)).enterNewView(newView.getNewViewNumber());
//     }

//     // /* 
//     //  * IncorrectTest3 - Incorrect, wrong checkpoint
//     //  * 2 missing Q
//     //  */
//     // @Test
// 	// void testRecvIncorrectNewView3() {
//     //     HbftJavaReplica spyReplica = Mockito.spy(replicaA);
//     //     RequestMessage request = new RequestMessage("123", 0, "C0");
//     //     RequestMessage request2 = new RequestMessage("321", 0, "C0");
//     //     long seqNumber = 1;
//     //     SpeculativeHistory history = new SpeculativeHistory();
//     //     history.addEntry(seqNumber, request);
//     //     ViewChangeMessage viewChange  = new ViewChangeMessage(2, history, new Checkpoint(history.getGreatestSeqNumber(), history), history.getRequests(), replicaC.getId());
//     //     ViewChangeMessage viewChange2  = new ViewChangeMessage(2, history, null, history.getRequests(), replicaD.getId());
//     //     ViewChangeMessage viewChange3  = new ViewChangeMessage(2, history, null, history.getRequests(), replicaD.getId());
//     //     Collection<ViewChangeMessage> viewChanges = new ArrayList<>();
//     //     viewChanges.add(viewChange);
//     //     viewChanges.add(viewChange2);
//     //     viewChanges.add(viewChange3);
        
//     //     NewViewMessage newView = new NewViewMessage(2, viewChanges, new Checkpoint(history.getGreatestSeqNumber(), history), history);
    
//     //     spyReplica.recvNewView(newView);
//     //     Assert.isTrue(!replicaA.getMessageLog().acceptNewView(newView, 1, logger), "Should not accept the new view!");
//     //     verify(spyReplica, times(0)).enterNewView(newView.getNewViewNumber());
//     // }

//     // /* 
//     //  * IncorrectTest4 - Incorrect, wrong checkpoint,
//     //  * 1 missing P
//     //  */
//     // @Test
// 	// void testRecvIncorrectNewView4() {
//     //     HbftJavaReplica spyReplica = Mockito.spy(replicaA);
//     //     RequestMessage request = new RequestMessage("123", 0, "C0");
//     //     RequestMessage request2 = new RequestMessage("321", 0, "C0");
//     //     long seqNumber = 1;
//     //     SpeculativeHistory history = new SpeculativeHistory();
//     //     history.addEntry(seqNumber, request);
//     //     ViewChangeMessage viewChange  = new ViewChangeMessage(2, history, new Checkpoint(history.getGreatestSeqNumber(), history), history.getRequests(), replicaC.getId());
//     //     ViewChangeMessage viewChange2  = new ViewChangeMessage(2, null, null, history.getRequests(), replicaD.getId());
//     //     ViewChangeMessage viewChange3  = new ViewChangeMessage(2, history, new Checkpoint(history.getGreatestSeqNumber(), history), history.getRequests(), replicaD.getId());
//     //     Collection<ViewChangeMessage> viewChanges = new ArrayList<>();
//     //     viewChanges.add(viewChange);
//     //     viewChanges.add(viewChange2);
//     //     viewChanges.add(viewChange3);
        
//     //     NewViewMessage newView = new NewViewMessage(2, viewChanges, new Checkpoint(history.getGreatestSeqNumber(), history), history);
    
//     //     spyReplica.recvNewView(newView);
//     //     Assert.isTrue(!replicaA.getMessageLog().acceptNewView(newView, 1, logger), "Should not accept the new view!");
//     //     verify(spyReplica, times(0)).enterNewView(newView.getNewViewNumber());
//     // }

//     @Test
// 	void testNewViewAdoption() {
//         HbftJavaReplica spyReplica = Mockito.spy(replicaA);
//         RequestMessage request = new RequestMessage("123", 0, "C0");
//         RequestMessage request2 = new RequestMessage("321", 0, "C0");
//         RequestMessage request3 = new RequestMessage("333", 0, "C0");
//         long seqNumber = 1;
//         SpeculativeHistory history = new SpeculativeHistory();
//         history.addEntry(seqNumber, request);
//         history.addEntry(seqNumber + 1, request2);
//         SpeculativeHistory history2 = new SpeculativeHistory();
//         history2.addEntry(seqNumber + 1, request2);
//         history2.addEntry(seqNumber + 3, request3);
//         ViewChangeMessage viewChange  = new ViewChangeMessage(2, null, null, history.getRequests(), replicaC.getId());
//         ViewChangeMessage viewChange2  = new ViewChangeMessage(2, null, null, history2.getRequests(), replicaD.getId());
//         ViewChangeMessage viewChange3  = new ViewChangeMessage(2, null, null, history2.getRequests(), replicaA.getId());
//         Collection<ViewChangeMessage> viewChanges = new ArrayList<>();
//         viewChanges.add(viewChange);
//         viewChanges.add(viewChange2);
//         viewChanges.add(viewChange3);

//         SpeculativeHistory history3 = new SpeculativeHistory();
//         history3.addEntry(seqNumber + 1, request2);
//         history3.addEntry(seqNumber + 3, request3);
//         history3.addEntry(seqNumber, null);
//         NewViewMessage newView = new NewViewMessage(2, viewChanges, new Checkpoint(0, null), history3);

//         SpeculativeHistory historyExpected = new SpeculativeHistory();
//         historyExpected.addEntry(seqNumber + 1, request2);
//         historyExpected.addEntry(seqNumber + 3, request3);
    
//         spyReplica.recvNewView(newView);
//         Assert.isTrue(replicaA.getMessageLog().acceptNewView(newView, 1, logger), "Should accept the new view!");
//         verify(spyReplica, times(1)).enterNewView(newView.getNewViewNumber());
//         System.out.println(replicaA.getSpeculativeHistory());
//         Assert.isTrue(replicaA.getSpeculativeHistory().equals(historyExpected), "History should exclude noop");
//     }


// }
