import {
  DeliveredMessagesList,
  DroppedMessagesList,
  QueuedMessagesList,
} from "@/components/messages";
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
        <Title order={3}>Queued Messages</Title>
        <QueuedMessagesList />
        <br />
        <Title order={3}>Delivered Messages</Title>
        <DeliveredMessagesList />
        <br />
        <Title order={3}>Dropped Messages</Title>
        <DroppedMessagesList />
      </Stack>
    </Container>
  );
}
