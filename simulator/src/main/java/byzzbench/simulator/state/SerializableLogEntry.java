package byzzbench.simulator.state;

import lombok.Data;

import java.io.Serializable;

@Data
public class SerializableLogEntry implements LogEntry {
    private final Serializable entry;
}
