---
layout: default
title: User Interface
---

# User Interface

ByzzBench includes a user interface that allows to visualize and control the execution of the protocol.

![Web UI Overview](ui-overview.png)

In the top section, under *Nodes* we can see the state of each Replica, as well as the list of messages currently
waiting to be delivered in their mailbox.

Under *Schedule*, we can see the events in the order they were delivered to their respective recipients. *Dropped
Messages* displays the set of events that failed to be delivered.

The UI allows to introspect the state of each node and message:

![UI Node Details](ui-node-details.png)

## How serialization works

Serialization of Java classes is handled by the [Spring Boot framework](https://spring.io/projects/spring-boot), using
the [Jackson library](https://docs.spring.io/spring-boot/reference/features/json.html).

By default, Jackson serializes all fields of a class, unless they are annotated with `@JsonIgnore`.

It will also include the values returned by Getters (methods that start with `get` or `is`), unless they are also
annotated with `@JsonIgnore`.
