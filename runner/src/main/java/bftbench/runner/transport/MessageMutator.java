package bftbench.runner.transport;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.util.Collection;
import java.util.function.Function;

@RequiredArgsConstructor
public abstract class MessageMutator implements Serializable, Function<Serializable, Serializable> {
    @Getter
    private final String name;

    @Getter
    private final Collection<Class<? extends Serializable>> inputClasses;
}
