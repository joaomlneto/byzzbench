package byzzbench.simulator.protocols.event_driven_hotstuff.mutator;

import byzzbench.simulator.config.ByzzBenchConfig;
import byzzbench.simulator.faults.FaultContext;
import byzzbench.simulator.faults.factories.MessageMutatorFactory;
import byzzbench.simulator.faults.faults.MessageMutationFault;
import byzzbench.simulator.protocols.event_driven_hotstuff.EDHSNode;
import byzzbench.simulator.protocols.event_driven_hotstuff.EDHSQuorumCertificate;
import byzzbench.simulator.protocols.event_driven_hotstuff.EDHotStuffReplica;
import byzzbench.simulator.protocols.event_driven_hotstuff.EDHotStuffScenario;
import byzzbench.simulator.protocols.event_driven_hotstuff.messages.NewViewMessage;
import byzzbench.simulator.protocols.event_driven_hotstuff.messages.GenericVote;
import byzzbench.simulator.protocols.event_driven_hotstuff.messages.NewViewMessage;
import byzzbench.simulator.protocols.event_driven_hotstuff.mutator.any_scope.ASGenericMutator;
import byzzbench.simulator.protocols.event_driven_hotstuff.mutator.any_scope.ASNewViewMutator;
import byzzbench.simulator.protocols.event_driven_hotstuff.mutator.small_scope.SSGenericMutator;
import byzzbench.simulator.protocols.event_driven_hotstuff.mutator.small_scope.SSNewViewMutator;
import byzzbench.simulator.transport.Event;
import byzzbench.simulator.transport.MessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.springframework.stereotype.Component;

import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;

@Component
@ToString
@RequiredArgsConstructor
public class NewViewMutatorFactory extends MessageMutatorFactory {
    private final ByzzBenchConfig config;

    @Override
    public List<MessageMutationFault> mutators() {
        if (config.isSmallScope()) return (new SSNewViewMutator()).getMutationFaults();
        else return (new ASNewViewMutator()).getMutationFaults();
    }
}
