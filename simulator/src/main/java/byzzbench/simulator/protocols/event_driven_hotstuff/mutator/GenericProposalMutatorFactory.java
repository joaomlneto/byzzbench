package byzzbench.simulator.protocols.event_driven_hotstuff.mutator;

import byzzbench.simulator.config.ByzzBenchConfig;
import byzzbench.simulator.faults.FaultContext;
import byzzbench.simulator.faults.factories.MessageMutatorFactory;
import byzzbench.simulator.faults.faults.MessageMutationFault;
import byzzbench.simulator.protocols.basic_hotstuff.QuorumCertificate;
import byzzbench.simulator.protocols.event_driven_hotstuff.*;
import byzzbench.simulator.protocols.event_driven_hotstuff.messages.GenericMessage;
import byzzbench.simulator.protocols.event_driven_hotstuff.mutator.any_scope.ASGenericMutator;
import byzzbench.simulator.protocols.event_driven_hotstuff.mutator.small_scope.SSGenericMutator;
import byzzbench.simulator.transport.Event;
import byzzbench.simulator.transport.MessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.springframework.stereotype.Component;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Component
@ToString
@RequiredArgsConstructor
public class GenericProposalMutatorFactory extends MessageMutatorFactory {
    private final ByzzBenchConfig config;

    @Override
    public List<MessageMutationFault> mutators() {
        if (config.isSmallScope()) return (new SSGenericMutator()).getMutationFaults();
        else return (new ASGenericMutator()).getMutationFaults();
    }
}
