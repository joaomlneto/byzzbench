"use client";

import AdoBStateDiagram from "@/components/adob/AdoBStateDiagram";
import { DoSchedulerActionIcon } from "@/components/DoSchedulerActionIcon";
import {
  DeliveredMessagesList,
  DroppedMessagesList,
} from "@/components/messages";
import { MutatorsList } from "@/components/MutatorsList";
import { NodeList } from "@/components/NodeList";
import { ResetActionIcon } from "@/components/ResetActionIcon";
import { Container, Group, Stack, Title } from "@mantine/core";
import React from "react";

export default function Home() {
  return (
    <Container fluid p="xl">
      <Stack gap="md">
        <Group gap="xs">
          <ResetActionIcon />
          <DoSchedulerActionIcon />
        </Group>
        <Title order={3}>Nodes</Title>
        <NodeList />
        <br />
        <Title order={3}>Schedule</Title>
        <DeliveredMessagesList />
        <br />
        <Title order={3}>Dropped Messages</Title>
        <DroppedMessagesList />
        <br />
        <Title order={3}>Message Mutators</Title>
        <MutatorsList />
        <br />
        <Title order={3}>AdoB State</Title>
        <AdoBStateDiagram />
      </Stack>
    </Container>
  );
}
