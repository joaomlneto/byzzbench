package byzzbench.simulator.scheduler;

import byzzbench.simulator.service.MessageMutatorService;
import byzzbench.simulator.utils.NonNull;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Abstract base class for a scheduler.
 */
@RequiredArgsConstructor
public abstract class BaseScheduler implements Scheduler {
  @NonNull @Getter private final String id;
  @NonNull
  @Getter(AccessLevel.PROTECTED)
  private final transient MessageMutatorService messageMutatorService;
  @Getter protected boolean dropMessages = true;

  /**
   * Loads the parameters for the scheduler from a JSON object.
   * @param parameters The JSON object containing the parameters for the
   *     scheduler.
   */
  public final void loadParameters(JsonNode parameters) {
    // check if drop messages
    if (parameters.has("dropMessages")) {
      this.dropMessages = parameters.get("dropMessages").asBoolean();
    }

    this.loadSchedulerParameters(parameters);
  }

  /**
   * Loads the subclass-specific parameters for the scheduler from a JSON
   * object.
   * @param parameters The JSON object containing the parameters for the
   *     scheduler.
   */
  protected abstract void loadSchedulerParameters(JsonNode parameters);
}
