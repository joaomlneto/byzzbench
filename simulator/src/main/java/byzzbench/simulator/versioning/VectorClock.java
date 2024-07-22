package byzzbench.simulator.versioning;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class VectorClock implements Serializable {

  private final TreeMap<String, Long> versions;

  public VectorClock() { this(List.of()); }

  public VectorClock(List<VectorClockEntry> entries) {
    versions = new TreeMap<>();
    for (VectorClockEntry entry : entries) {
      versions.put(entry.getNodeId(), entry.getVersion());
    }
  }

  /**
   * Only used for cloning
   *
   * @param versionMap The version map
   */
  private VectorClock(TreeMap<String, Long> versionMap) {
    if (versionMap == null) {
      throw new IllegalArgumentException("Invalid version map");
    }

    this.versions = versionMap;
  }

  @Override
  public VectorClock clone() {
    return new VectorClock(new TreeMap<>(this.versions));
  }

  public void incrementVersion(String node) {
    if (node == null || node.isEmpty()) {
      throw new IllegalArgumentException("Invalid node or time");
    }

    versions.compute(node,
                     (k, version) -> (version == null) ? 1 : (version + 1));
  }

  public VectorClock incremented(int nodeId, long time) {
    VectorClock copyClock = this.clone();
    copyClock.incrementVersion(String.valueOf(nodeId));
    return copyClock;
  }

  /**
   * Merge this clock with another clock.
   *
   * @param otherClock The other clock
   * @return The merged clock
   * @see <a href="https://en.wikipedia.org/wiki/Vector_clock">Vector Clock</a>
   */
  public VectorClock merge(VectorClock otherClock) {
    if (otherClock == null) {
      throw new IllegalArgumentException("Invalid clock");
    }

    VectorClock newClock = new VectorClock();

    // Copy the versions from this clock
    newClock.versions.putAll(this.versions);

    // Merge the versions from the otherClock
    // Take the maximum of the two versions if they both exist
    for (Map.Entry<String, Long> entry : otherClock.versions.entrySet()) {
      newClock.versions.merge(entry.getKey(), entry.getValue(), Math::max);
    }

    return newClock;
  }

  public Occurred compare(VectorClock otherClock) {
    if (otherClock == null) {
      throw new IllegalArgumentException("Invalid clock");
    }

    boolean thisIsLess = false;
    boolean otherIsLess = false;

    for (Map.Entry<String, Long> entry : this.versions.entrySet()) {
      Long otherVersion = otherClock.versions.get(entry.getKey());
      if (otherVersion == null) {
        otherIsLess = true;
      } else if (entry.getValue() > otherVersion) {
        thisIsLess = true;
      } else if (entry.getValue() < otherVersion) {
        otherIsLess = true;
      }
    }

    for (Map.Entry<String, Long> entry : otherClock.versions.entrySet()) {
      if (!this.versions.containsKey(entry.getKey())) {
        thisIsLess = true;
        break;
      }
    }

    if (thisIsLess && otherIsLess) {
      return Occurred.CONCURRENTLY;
    } else if (thisIsLess) {
      return Occurred.BEFORE;
    } else if (otherIsLess) {
      return Occurred.AFTER;
    } else {
      return Occurred.EQUAL;
    }
  }
}
