"use client";

import { ClientList } from "@/components/ClientList";
import { DroppedMessagesList } from "@/components/Events";
import { ScenarioEnabledFaultsList } from "@/components/FaultsList";
import { NodeList } from "@/components/NodeList";
import { PredicateList } from "@/components/PredicateList";
import { RunningSimulatorStats } from "@/components/RunningSimulatorStats";
import { ScheduleDetails } from "@/components/Schedule";
import { useGetMode, useGetSchedule } from "@/lib/byzzbench-client";
import {
  Accordion,
  Container,
  Group,
  ScrollArea,
  Stack,
  Text,
} from "@mantine/core";
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

  const { data: schedule } = useGetSchedule();

  const mode = useGetMode();

  if (mode.data?.data === "RUNNING") {
    return (
      <Container fluid p="xl">
        <RunningSimulatorStats />
      </Container>
    );
  }

  return (
    <Container fluid p="xl">
      <Stack gap="md">
        <Accordion
          multiple
          variant="separated"
          value={selectedAccordionEntries}
          onChange={setSelectedAccordionEntries}
        >
          <Group wrap="nowrap" gap="xs">
            <Text>Invariants:</Text>
            <PredicateList />
          </Group>
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
              <ScrollArea h={250} type="auto">
                {schedule?.data && (
                  <ScheduleDetails
                    hideTitle
                    hideMaterializeButton
                    hideDownloadButton
                    hideDetailsButton
                    hideScenario
                    title="Current Schedule"
                    schedule={schedule.data}
                  />
                )}
              </ScrollArea>
            </Accordion.Panel>
          </Accordion.Item>
          <Accordion.Item key="dropped_msgs" value="dropped_msgs">
            <Accordion.Control>Discarded Events</Accordion.Control>
            <Accordion.Panel>{<DroppedMessagesList />}</Accordion.Panel>
          </Accordion.Item>
          <Accordion.Item key="faults" value="faults">
            <Accordion.Control>Network Faults</Accordion.Control>
            <Accordion.Panel>
              <ScenarioEnabledFaultsList />
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
