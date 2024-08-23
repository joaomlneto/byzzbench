package byzzbench.simulator.state;

import java.io.Serializable;
import lombok.Data;

@Data
public class SerializableLogEntry implements LogEntry {
  private final Serializable entry;
}
