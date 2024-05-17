package bftbench.runner.transport;

import java.io.Serializable;

public interface Event extends Serializable {
    long getEventId();

    String toString();
}
