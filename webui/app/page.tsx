"use client";

import {ClientList} from "@/components/ClientList";
import {DroppedMessagesList} from "@/components/Events";
import {ScenarioEnabledFaultsList} from "@/components/FaultsList";
import {NodeList} from "@/components/NodeList";
import {PredicateList} from "@/components/PredicateList";
import {RunningSimulatorStats} from "@/components/RunningSimulatorStats";
import {ScheduleDetails} from "@/components/Schedule";
import {ScenarioScheduledFaultsList} from "@/components/ScheduledFaultsList";
import {useGetMode, useGetSchedule} from "@/lib/byzzbench-client";
import {Accordion, AppShell, Container, Group, ScrollArea, Stack, Title,} from "@mantine/core";
import {useLocalStorage} from "@mantine/hooks";
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

    const {data: schedule} = useGetSchedule();

    const mode = useGetMode();

    if (mode.data?.data === "RUNNING") {
        return (
            <Container fluid p="xl">
                <RunningSimulatorStats/>
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
                    <Group wrap="nowrap" gap="xs" align="center">
                        <Title order={3}>{schedule?.data.scenarioId}</Title>
                        <PredicateList/>
                    </Group>
                    <Accordion.Item key="clients" value="clients">
                        <Accordion.Control>Clients</Accordion.Control>
                        <Accordion.Panel>
                            <ClientList/>
                        </Accordion.Panel>
                    </Accordion.Item>
                    <Accordion.Item key="nodes" value="nodes">
                        <Accordion.Control>Nodes</Accordion.Control>
                        <Accordion.Panel>
                            <NodeList/>
                        </Accordion.Panel>
                    </Accordion.Item>
                    <Accordion.Item key="adob" value="adob">
                        <Accordion.Control>AdoB State</Accordion.Control>
                        <Accordion.Panel>
                            <AdoBStateDiagram/>
                        </Accordion.Panel>
                    </Accordion.Item>
                </Accordion>
            </Stack>

            <AppShell.Aside p="md" maw={400}>
                <Stack gap="xs">
                    <Title order={5}>Schedule</Title>
                    <ScrollArea mah={500} type="auto">
                        {schedule?.data && (
                            <ScheduleDetails
                                hideTitle
                                hideMaterializeButton
                                hideDownloadButton
                                hideDetailsButton
                                hideScenario
                                hideSaveButton
                                title="Current Schedule"
                                schedule={schedule.data}
                            />
                        )}
                    </ScrollArea>
                    <ScrollArea type="auto" mah={500} >
                    <Title order={5}>Trigger Faulty Behaviors</Title>
                    <ScenarioEnabledFaultsList/>
                    <Title order={5}>Scheduled Faults</Title>
                    <ScenarioScheduledFaultsList/>
                    <Title order={5}>Discarded Events</Title>
                    <DroppedMessagesList/>
                    </ScrollArea>
                </Stack>
             </AppShell.Aside>
        </Container>
    );
}
