"use client";

import { ClientList } from "@/components/ClientList";
import {
  DeliveredMessagesList,
  DroppedMessagesList,
} from "@/components/messages";
import { MutatorsList } from "@/components/MutatorsList";
import { NodeList } from "@/components/NodeList";
import { ScheduleList } from "@/components/ScheduleList";
import { Accordion, Container, Stack } from "@mantine/core";
import { useLocalStorage } from "@mantine/hooks";
import dynamic from "next/dynamic";
import React from "react";

const AdoBStateDiagram = dynamic<{}>(
  () =>
    import("@/components/adob/AdoBStateDiagram").then(
      (m) => m.AdoBStateDiagram,
    ),
  {
    ssr: false,
  },
);

export default function Home() {
  const [selectedAccordionEntries, setSelectedAccordionEntries] =
    useLocalStorage<string[]>({
      key: "byzzbench/selectedAccordionEntries",
      defaultValue: ["nodes", "schedule"],
    });

  return (
    <Container fluid p="xl">
      <Stack gap="md">
        <Accordion
          multiple
          variant="separated"
          value={selectedAccordionEntries}
          onChange={setSelectedAccordionEntries}
        >
          <Accordion.Item key="clients" value="clients">
            <Accordion.Control>Clients</Accordion.Control>
            <Accordion.Panel>
              <ClientList />
            </Accordion.Panel>
          </Accordion.Item>
          <Accordion.Item key="saved_schedules" value="saved_schedules">
            <Accordion.Control>Saved Schedules</Accordion.Control>
            <Accordion.Panel>
              <ScheduleList />
            </Accordion.Panel>
          </Accordion.Item>
          <Accordion.Item key="nodes" value="nodes">
            <Accordion.Control>Nodes</Accordion.Control>
            <Accordion.Panel>
              <NodeList />
            </Accordion.Panel>
          </Accordion.Item>
          <Accordion.Item key="schedule" value="schedule">
            <Accordion.Control>Schedule</Accordion.Control>
            <Accordion.Panel>
              <DeliveredMessagesList />
            </Accordion.Panel>
          </Accordion.Item>
          <Accordion.Item key="dropped_msgs" value="dropped_msgs">
            <Accordion.Control>Dropped Messages</Accordion.Control>
            <Accordion.Panel>
              <DroppedMessagesList />
            </Accordion.Panel>
          </Accordion.Item>
          <Accordion.Item key="mutators" value="mutators">
            <Accordion.Control>Message Mutators</Accordion.Control>
            <Accordion.Panel>
              <MutatorsList />
            </Accordion.Panel>
          </Accordion.Item>
          <Accordion.Item key="adob" value="adob">
            <Accordion.Control>AdoB State</Accordion.Control>
            <Accordion.Panel>
              <AdoBStateDiagram />
            </Accordion.Panel>
          </Accordion.Item>
        </Accordion>
      </Stack>
    </Container>
  );
}
