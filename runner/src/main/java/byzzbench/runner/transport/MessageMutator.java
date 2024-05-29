package byzzbench.runner.transport;

import io.micronaut.serde.annotation.Serdeable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.Collection;
import java.util.function.Function;

@Getter
@RequiredArgsConstructor
@Serdeable
@ToString
public abstract class MessageMutator implements Serializable, Function<Serializable, Serializable> {
    private final String name;

    private final Collection<Class<? extends Serializable>> inputClasses;
}
