package byzzbench.runner.controller;

import byzzbench.runner.transport.MessageMutator;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Data;

import java.util.Collection;

@Data
@Serdeable
public class MutatorDTO {
    private String name;
    private Collection<String> inputClasses;

    public MutatorDTO(MessageMutator messageMutator) {
        this.name = messageMutator.getName();
        this.inputClasses = messageMutator.getInputClasses().stream().map(Class::getSimpleName).toList();
    }
}
