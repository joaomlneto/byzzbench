package byzzbench.simulator;

import byzzbench.simulator.schedule.Schedule;
import byzzbench.simulator.scheduler.Scheduler;
import byzzbench.simulator.state.adob.AdobDistributedState;
import byzzbench.simulator.transport.Transport;
import java.util.List;
import java.util.Set;

public interface Scenario {
  /**
   * Get a unique identifier for the scenario.
   * @return A unique identifier for the scenario.
   */
  String getId();

  /**
   * Run the scenario.
   */
  void runScenario();

  /**
   * Get the scheduler for the scenario.
   * @return The scheduler for the scenario.
   */
  Scheduler getScheduler();

  /**
   * Get the transport layer for the scenario.
   * @return The transport layer for the scenario.
   */
  Transport getTransport();

  /**
   * Get the schedule for the scenario.
   * @return The schedule for the scenario.
   */
  Schedule getSchedule();

  /**
   * Get the invariants that must be satisfied by the scenario at all times.
   * @return The invariants that must be satisfied by the scenario at all times.
   */
  List<ScenarioPredicate> getInvariants();

  void setupScenario();
  AdobDistributedState getAdobOracle();
  TerminationCondition getTerminationCondition();
  boolean invariantsHold();
  Set<ScenarioPredicate> unsatisfiedInvariants();
}
