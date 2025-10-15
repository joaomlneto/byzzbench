"use client";

import {DroppedMessagesList} from "@/components/Events";
import {ScheduleDetails} from "@/components/Schedule";
import {ScenarioScheduledFaultsList} from "@/components/ScheduledFaultsList";
import {useGetScenario, useGetSchedule} from "@/lib/byzzbench-client";
import {AppShell, ScrollArea, Stack, Title} from "@mantine/core";
import React from "react";

export type ScenarioAsideProps = {
    scenarioId: number;
};

export const ScenarioAside = ({scenarioId}: ScenarioAsideProps) => {
    const {data} = useGetScenario(scenarioId);
    const {data: scheduleData} = useGetSchedule(data?.data.scheduleId ?? 0, {
        query: {
            enabled: data?.data.scheduleId !== undefined,
        },
    });

    return (
        <AppShell.Aside p="md" maw={400}>
            <ScrollArea type="never" mah="100vh">
                <Stack gap="xs">
                    <Title order={5}>Schedule</Title>
                    <ScrollArea mah={500} type="always" style={{overflowY: "auto"}}>
                        <div style={{maxHeight: "500px", overflowY: "auto"}}>
                            {scheduleData && data?.data.scheduleId && (
                                <ScheduleDetails
                                    hideTitle
                                    hideParameters
                                    hideMaterializeButton
                                    hideDownloadButton
                                    hideDetailsButton
                                    hideScenario
                                    hideSaveButton
                                    title="Current Schedule"
                                    schedule={scheduleData.data}
                                />
                            )}
                        </div>
                    </ScrollArea>
                    <Title order={5}>ScheduledFaults</Title>
                    <ScenarioScheduledFaultsList scenarioId={scenarioId}/>
                    <Title order={5}>Discarded Events</Title>

                    <ScrollArea mah={500} type="always" style={{overflowY: "auto"}}>
                        <DroppedMessagesList scenarioId={scenarioId}/>
                    </ScrollArea>
                </Stack>
            </ScrollArea>
        </AppShell.Aside>
    );
};
