package byzzbench.simulator.scheduler.twins;

import byzzbench.simulator.Replica;
import byzzbench.simulator.state.TotalOrderCommitLog;
import byzzbench.simulator.transport.MessagePayload;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.extern.java.Log;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * A Twins {@link Replica} that emulates byzantine behavior
 * by switching between different replica instances behind the scenes.
 * <p>
 * See "Twins: BFT Systems Made Robust" by Shehar Bano, Alberto Sonnino,
 * Andrey Chursin, Dmitri Perelman, Zekun Li, Avery Ching and Dahlia Malkhi.
 * https://drops.dagstuhl.de/entities/document/10.4230/LIPIcs.OPODIS.2021.7
 */
@Log
public class TwinsReplica extends Replica {
    @JsonIgnore
    private final TwinsTransport twinsTransport;
    /**
     * The list of replicas that are part of this Twins replica.
     */
    @Getter
    private final ArrayList<Replica> replicas = new ArrayList<>();

    /**
     * Create a new Twins replica.
     *
     * @param replica  the replica to clone
     * @param numTwins the number of twins to create
     */
    public TwinsReplica(Replica replica, int numTwins) {
        super(replica.getId(), replica.getScenario(), new TotalOrderCommitLog());

        this.twinsTransport = new TwinsTransport(replica.getScenario());

        if (numTwins < 2) {
            throw new IllegalArgumentException("numTwins must be at least 2");
        }

        // create the twin copies
        replicas.add(replica);
        for (int i = 1; i < numTwins; i++) {
            Replica twin = replica.getScenario().cloneReplica(replica);
            replicas.add(twin);
            twin.initialize();
        }
    }

    @Override
    public void handleClientRequest(String clientId, Serializable request) throws Exception {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public void handleMessage(String sender, MessagePayload message) throws Exception {
        throw new UnsupportedOperationException("Not yet implemented!");
    }
}
