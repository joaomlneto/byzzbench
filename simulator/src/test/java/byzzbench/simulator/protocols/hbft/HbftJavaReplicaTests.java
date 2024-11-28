package byzzbench.simulator.protocols.hbft;

import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert;

import byzzbench.simulator.protocols.hbft.message.PrepareMessage;
import byzzbench.simulator.protocols.hbft.message.RequestMessage;
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
	void testRecvWrongPrepare() {
        long viewNumber = 1;
        long seqNumber = 2;
        RequestMessage request = new RequestMessage("123", 0, "C0");
        byte[] digest = replica.digest(request);
        PrepareMessage prepare = new PrepareMessage(viewNumber, seqNumber, digest, request);
        
        replica.recvPrepare(prepare);

        // Prepare message should not be accepted so ticket should be null
        Assert.isTrue(replica.getMessageLog().getTicket(viewNumber, seqNumber) == null, "Prepare message with incorrect seqNum not accepted!");
    }
    
    // @Test
	// void testRecvCommit() {
    //     long viewNumber = 1;
    //     long seqNumber = 1;
    //     RequestMessage request = new RequestMessage("123", 0, "C0");
    //     byte[] digest = replica.digest(request);
    //     PrepareMessage prepare = new PrepareMessage(viewNumber, seqNumber, digest, request);
    //     CommitMessage commit = new CommitMessage(viewNumber, seqNumber, digest, request, "B", this.replica.getSpeculativeHistory());

    //     replica.recvPrepare(prepare);

    //     // Prepare message should be accepted
    //     Assert.isTrue(replica.getMessageLog().getTicket(viewNumber, seqNumber).getMessages().contains(commit), "Commit message with correct seqNum and viewNum should be accepted!");
    //     Assert.isTrue(!replica.getMessageLog().getTicket(viewNumber, seqNumber).isCommittedLocal(tolerance), "Commit message with correct seqNum and viewNum should be accepted!");
    // }
}
