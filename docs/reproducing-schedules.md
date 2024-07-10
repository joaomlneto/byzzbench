# Reproducing Schedules

ByzzBench ensures that the execution of distributed systems is fully deterministic. This means that given the same
initial state and sequence of events, the system will always produce the same result. This determinism is crucial for
debugging and verifying the behavior of distributed systems.

![A Schedule](schedule.png)
*Example of a schedule in ByzzBench*

In addition to simply delivering events, ByzzBench enables users to simulate various types of faults to test the
robustness of their distributed systems. Users can:

- **Drop Messages**: Prevent a message from being delivered to its intended recipient, simulating network failures or
  message
  loss.
- **Mutate Messages**: Modify the contents of a message before it is delivered, simulating Byzantine faults and other
  message
  corruption scenarios.This approach is inspired by the [ByzzFuzz](https://dl.acm.org/doi/abs/10.1145/3586053) approach
  to simulating Byzantine faults.

![Mutating a Message](mutate-message.png)

## Additional replay capabilities

In the future, schedules will be recorded and serialized by the framework, and the UI will include the functionality to
reproduce them.
