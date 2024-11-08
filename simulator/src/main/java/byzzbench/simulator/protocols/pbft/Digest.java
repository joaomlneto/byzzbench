package byzzbench.simulator.protocols.pbft;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import lombok.Getter;

/**
 * A digest of a request
 */
@Getter
public class Digest implements Serializable, Comparable<Digest> {
  private final byte[] value;

  public Digest(byte[] value) { this.value = value; }

  public Digest(String value) { this.value = value.getBytes(); }

  @Override
  public int compareTo(@NotNull Digest other) {
    if (other == null) {
      return 1;
    }
    for (int i = 0; i < value.length; i++) {
      if (value[i] != other.value[i]) {
        return value[i] - other.value[i];
      }
    }
    return 0;
  }
}
