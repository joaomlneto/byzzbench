package byzzbench.simulator.faults;

import byzzbench.simulator.utils.NonNull;

import java.io.Serializable;
import java.util.function.Consumer;

public interface FaultBehavior extends Consumer<ScenarioContext>, Serializable {
    @NonNull
    String getId();

    @NonNull
    String getName();
}
