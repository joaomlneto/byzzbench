"use client";

import AdoBStateDiagram from "@/components/adob/AdoBStateDiagram";
import { ClientList } from "@/components/ClientList";
import {
  DeliveredMessagesList,
  DroppedMessagesList,
} from "@/components/messages";
import { MutatorsList } from "@/components/MutatorsList";
import { NodeList } from "@/components/NodeList";
import { useSavedSchedulesStore } from "@/hooks/useSavedSchedules";
import { Accordion, Container, JsonInput, Stack } from "@mantine/core";
import { useLocalStorage } from "@mantine/hooks";
import React from "react";

export default function Home() {
  const [selectedAccordionEntries, setSelectedAccordionEntries] =
    useLocalStorage<string[]>({
      key: "byzzbench/selectedAccordionEntries",
      defaultValue: ["nodes", "schedule"],
    });

  const schedules = useSavedSchedulesStore((x) => x.schedules);

  return (
    <Container fluid p="xl">
      <Stack gap="md">
        <Container fluid>
          <JsonInput
            value={JSON.stringify(schedules, null, 2)}
            autosize
            maxRows={30}
          />
        </Container>
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
