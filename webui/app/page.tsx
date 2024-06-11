"use client";

import {
  DeliveredMessagesList,
  DroppedMessagesList,
} from "@/components/messages";
import { MutatorsList } from "@/components/MutatorsList";
import { NodeList } from "@/components/NodeList";
import { ResetActionIcon } from "@/components/ResetActionIcon";
import { Container, Stack, Title } from "@mantine/core";
import React from "react";

export default function Home() {
  return (
    <Container fluid p="xl">
      <Stack gap="md">
        <ResetActionIcon />
        <Title order={3}>Nodes</Title>
        <NodeList />
        <br />
        <Title order={3}>Delivered Messages</Title>
        <DeliveredMessagesList />
        <br />
        <Title order={3}>Dropped Messages</Title>
        <DroppedMessagesList />
        <br />
        <Title order={3}>Message Mutators</Title>
        <MutatorsList />
      </Stack>
    </Container>
  );
}
