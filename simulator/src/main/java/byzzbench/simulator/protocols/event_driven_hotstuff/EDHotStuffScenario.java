package byzzbench.simulator.protocols.event_driven_hotstuff;

import byzzbench.simulator.BaseScenario;
import byzzbench.simulator.LeaderBasedProtocolReplica;
import byzzbench.simulator.Replica;
import byzzbench.simulator.scheduler.Scheduler;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.extern.java.Log;

import java.util.*;
import java.util.stream.Collectors;

@Log
public class EDHotStuffScenario extends BaseScenario {
    private final int NUM_NODES = 4;
    @Getter
    private EDHSScenarioState state;

    public EDHotStuffScenario(Scheduler scheduler) {
        super("ed-hotstuff", scheduler);
    }

    private ArrayList<String> logs;// = new ArrayList<>();

    private HashSet<Long> commitViewSet;

    private HashSet<Long> nonSyncTimeoutViews;

    private HashMap<String, EDHotStuffReplica> replicaMap ;

    private HashSet<Long> viewsWithNetworkFaults;

    private HashMap<Long, HashSet<String>> processFaultsPerView;

    private ArrayList<EDHotStuffClient> clients;

    @Override
    protected void loadScenarioParameters(JsonNode parameters) {

    }

    @Override
    protected void setup() {
        state = new EDHSScenarioState();
        commitViewSet = new HashSet<>();
        logs = new ArrayList<>();
        nonSyncTimeoutViews = new HashSet<>();
        replicaMap = new HashMap<>();
        viewsWithNetworkFaults = new HashSet<>();
        processFaultsPerView = new HashMap<>();
        clients = new ArrayList<>();
        try {
            ArrayList<String> nodeIds = new ArrayList<>();
            for (int i = 0; i < NUM_NODES; i++) nodeIds.add(Character.toString((char) ('A' + i)));

            nodeIds.forEach(nodeId -> {
                EDHotStuffReplica replica = new EDHotStuffReplica(nodeId, this, nodeIds);
                System.out.println(nodeId);
                this.addNode(replica);
                replicaMap.put(nodeId, replica);
            });

            EDHotStuffClient client = new EDHotStuffClient(this, "Client_A");
            transport.addImmuneNode(client.getId());
            clients.add(client);

            this.addClient(client);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void run() {
        /*try {
            this.setNumClients(1);
            this.transport.sendClientRequest("C0", "123", "A");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }*/
    }

    public HashSet<ClientRequest> getPendingRequests() {
        ArrayList<ClientRequest> pendingRequests = new ArrayList<>();
        clients.forEach(c -> pendingRequests.addAll(c.getPendingRequests().values()));
        return new HashSet<>(pendingRequests);
    }

    public void log(String message) {
        //System.out.println(message);
        logs.add(message);
    }

    public ArrayList<String> getLogs() {
        return new ArrayList<>(logs);
    }

    public void registerCommit(long viewNumber) {
        commitViewSet.add(viewNumber);
        state.setCommitedNodes(replicaMap.values().stream().map(r -> r.getCommitLog().getLength()).max(Comparator.comparingInt(a -> a)).get());
    }

    public boolean hasCommitForView(long viewNumber) {
        return commitViewSet.contains(viewNumber);
    }

    public void registerNonSyncTimeout(String replicaId) {
        EDHotStuffReplica replica = replicaMap.get(replicaId);
        nonSyncTimeoutViews.add(replica.getViewNumber());
    }

    public boolean hasNonSyncTimeoutForView(long viewNumber) {
        return nonSyncTimeoutViews.contains(viewNumber);
    }

    public void registerNetworkFault(long viewNumber) {
        viewsWithNetworkFaults.add(viewNumber);

        state.setValidAssumptions(false);
    }

    public void registerFaultyReplica(long viewNumber, String faultyReplicaId) {
        if(!processFaultsPerView.containsKey(viewNumber)) processFaultsPerView.put(viewNumber, new HashSet<>());
        HashSet<String> faultyReplicas = processFaultsPerView.get(viewNumber);
        faultyReplicas.add(faultyReplicaId);

        int f = replicaMap.values().iterator().next().getMaxFaultyReplicas();
        if(processFaultsPerView.values().stream().anyMatch(fs -> fs.size() > f)) {
            state.setValidAssumptions(false);
            System.out.println("Scenario invalid. " + processFaultsPerView);
        }
    }

    public boolean isViewFaulty(long viewNumber) {
        EDHotStuffReplica replica = replicaMap.values().iterator().next();
        String leaderId = replica.getPacemaker().getLeaderId(viewNumber);
        if(!processFaultsPerView.containsKey(viewNumber)) processFaultsPerView.put(viewNumber, new HashSet<>());
        HashSet<String> faultyReplicas = processFaultsPerView.get(viewNumber);
        return viewsWithNetworkFaults.contains(viewNumber) || faultyReplicas.contains(leaderId) || (faultyReplicas.size() > replicaMap.size() - replica.getMinValidVotes());
    }

    public void onViewChange() {
        Collection<Long> replicaViews = replicaMap.values().stream().map(LeaderBasedProtocolReplica::getViewNumber).toList();
        state.setMinView(replicaViews.stream().min(Comparator.comparingLong(a -> a)).get());
        state.setMaxView(replicaViews.stream().max(Comparator.comparingLong(a -> a)).get());
    }

    @Override
    public int maxFaultyReplicas(int n) {
        return (int) Math.floor((double) (n - 1) / 3);
    }
}
