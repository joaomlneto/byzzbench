package byzzbench.simulator.protocols.fab2;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.nodes.Client;
import byzzbench.simulator.protocols.fab.FastByzantineReplica;
import byzzbench.simulator.protocols.fab.Role;
import byzzbench.simulator.protocols.pbft.message.RequestMessage;
import byzzbench.simulator.transport.DefaultClientReplyPayload;
import byzzbench.simulator.transport.MessagePayload;
import lombok.extern.java.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Log
//public class FastByzantineClient extends Client {
//    private final AtomicLong learners = new AtomicLong(0);
//    private final AtomicBoolean isFinished = new AtomicBoolean(false);
//    private final List<String> learnersList = new ArrayList<>();
//    private final List<String> proposersList = new ArrayList<>();
//    /**
//     * The replies received by the client.
//     */
//    private final List<Serializable> replies = new ArrayList<>();
//    /**
//     * The sequence number of the next request to be sent by the client.
//     */
//    private final AtomicLong requestSequenceNumber = new AtomicLong(0);
//    /**
//     * The maximum number of requests that can be sent by the client.
//     */
//    private final long maxRequests = 1000;
//
//    public FastByzantineClient(Scenario scenario, String id, List<String> learnersList, List<String> proposersList) {
//        super(scenario, id);
//        this.learnersList.addAll(learnersList);
//        this.proposersList.addAll(proposersList);
//    }
//
//    @Override
//    public void sendRequest(String senderId) {
//        long sequenceNumber = this.getRequestSequenceNumber().getAndIncrement();
//        String command = String.format("%s/%d", this.getId(), sequenceNumber);
//        // TODO: compute the digest
//        RequestMessage request = new RequestMessage(this.getId(), sequenceNumber, "-1", command);
//        this.getScenario().getTransport().sendClientRequest(this.getId(), request, senderId);
//    }
//
//    @Override
//    public void handleMessage(String senderId, MessagePayload reply) {
//        log.info("Client received a message from " + senderId);
//
//        this.replies.add(reply);
//        if (learnersList.contains(senderId)) {
//            learners.incrementAndGet();
//            if (learners.get() == 4) {
//                for (String proposer : proposersList) {
//                    this.sendRequest(proposer);
//                }
//
//                learners.set(0);
//            }
//        } else {
//            log.info("Client received a message from a non-learner node");
//        }
//    }
//}

public class
FastByzantineClient extends Client {
    private final AtomicLong learners = new AtomicLong(0);
    private final AtomicBoolean isFinished = new AtomicBoolean(false);
    private final List<String> learnersList = new ArrayList<>();
    private final List<String> proposersList = new ArrayList<>();
    /**
     * The replies received by the client.
     */
    private final List<Serializable> replies = new ArrayList<>();
    /**
     * The sequence number of the next request to be sent by the client.
     */
    private final AtomicLong requestSequenceNumber = new AtomicLong(0);
    /**
     * The maximum number of requests that can be sent by the client.
     */
    private final long maxRequests = 1000;

    public FastByzantineClient(Scenario scenario, String id, List<String> learnersList, List<String> proposersList) {
        super(scenario, id);
        this.learnersList.addAll(learnersList);
        this.proposersList.addAll(proposersList);
    }

    @Override
    public void sendRequest(String requestId, String senderId) {
        long sequenceNumber = this.getRequestSequenceNumber().get() - 1;
        // TODO: compute the digest
        RequestMessage request = new RequestMessage(this.getId(), sequenceNumber, "-1", requestId);
        this.getScenario().getTransport().sendMessage(this, request, senderId);
    }

    @Override
    public void handleMessage(String senderId, MessagePayload reply) {
        log.info("Client received a message from " + senderId);

        this.replies.add(reply);
        if (learnersList.contains(senderId)) {
            learners.incrementAndGet();
            if (learners.get() == 3) {
                for (String proposer : proposersList) {
                    this.sendRequest(this.generateRequestId(), proposer);
                }

                learners.set(0);
            }
        } else {
            log.info("Client received a message from a non-learner node");
        }
    }

    @Override
    public boolean isRequestCompleted(DefaultClientReplyPayload message) {
        Scenario s = this.getScenario();

        long matchingReplies = this.getReplies().get(message.getRequestId()).stream().filter(
                other -> other.equals(message.getReply())
        ).count();

        long numLearners = s.getReplicas().values().stream().filter(
                        // is FaB replica
                        byzzbench.simulator.protocols.fab.FastByzantineReplica.class::isInstance
                )
                .map(FastByzantineReplica.class::cast)
                .filter(
                        // replica is learner
                        replica -> replica.getRoles().contains(Role.LEARNER)
                )
                .count();

        // check if got f + 1 matching replies from learners
        return matchingReplies >= ((numLearners - 1) / 3) + 1;
    }
}
