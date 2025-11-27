"use client";

import { ScenarioActionList, StrategyActionList } from "@/components/Action";
import { DoSchedulerActionIcon } from "@/components/ActionIcon";
import { DroppedMessagesList } from "@/components/Events";
import { ScenarioFaultsList } from "@/components/FaultsList";
import { NodeList } from "@/components/NodeList";
import { PredicateList } from "@/components/PredicateList";
import {
  ScenarioCommitLogSummary,
  ShowMailboxesSwitch,
} from "@/components/Scenario";
import { ScheduleDetails } from "@/components/Schedule";
import { SchedulerScenarioMetadata } from "@/components/Scheduler";
import { useGetScenario, useGetSchedule } from "@/lib/byzzbench-client";
import {
  Anchor,
  Badge,
  Box,
  Card,
  Group,
  JsonInput,
  SimpleGrid,
  Stack,
  Tabs,
  Title,
} from "@mantine/core";
import Link from "next/link";
import React from "react";

export type ScenarioDebuggerProps = {
  scenarioId: number;
};

// A modern, debugger-like view for scenarios, organized in tabs and reusing existing widgets
export const ScenarioDebugger = ({ scenarioId }: ScenarioDebuggerProps) => {
  const { data: scenarioResponse } = useGetScenario(scenarioId);
  const scenario = scenarioResponse?.data;

  const { data: scheduleResp } = useGetSchedule(scenario?.scheduleId ?? 0, {
    query: { enabled: scenario?.scheduleId !== undefined },
  });

  const [selectedStrategy, setSelectedStrategy] = React.useState<string | null>(
    null,
  );

  return (
    <Stack gap="md">
      {/* Header */}
      <Card>
        <Card.Section p="md">
          <Stack gap="xs">
            <Group wrap="nowrap" gap="sm" align="center">
              <Title order={3} style={{ lineHeight: 1 }}>
                {scenario?.description ?? "Scenario"}
              </Title>
              {scenario?.scheduleId && (
                <Badge variant="light" color="blue">
                  <Anchor
                    component={Link}
                    href={`/schedules/${scenario.scheduleId}`}
                  >
                    Schedule {scenario.scheduleId}
                  </Anchor>
                </Badge>
              )}
              {scenario?.campaignId && (
                <Badge variant="light" color="grape">
                  <Anchor
                    component={Link}
                    href={`/campaigns/${scenario.campaignId}`}
                  >
                    Campaign {scenario.campaignId}
                  </Anchor>
                </Badge>
              )}
            </Group>

            <Group gap="sm" align="center" justify="space-between" wrap="wrap">
              <PredicateList scenarioId={scenarioId} />
              <ShowMailboxesSwitch />
            </Group>

            <ScenarioCommitLogSummary scenarioId={scenarioId} />
          </Stack>
        </Card.Section>
      </Card>

      {/* Tabs layout */}
      <Tabs defaultValue="overview" keepMounted={false} variant="outline">
        <Tabs.List>
          <Tabs.Tab value="overview">Overview</Tabs.Tab>
          <Tabs.Tab value="actions">Actions</Tabs.Tab>
          <Tabs.Tab value="nodes">Nodes</Tabs.Tab>
          <Tabs.Tab value="faults">Faults</Tabs.Tab>
          <Tabs.Tab value="dropped">Dropped</Tabs.Tab>
          <Tabs.Tab value="schedule">Schedule</Tabs.Tab>
        </Tabs.List>

        <Tabs.Panel value="overview" pt="md">
          <Card withBorder>
            <Card.Section p="md">
              <JsonInput
                label="Scenario"
                variant="unstyled"
                inputSize="xs"
                size="xs"
                autosize
                readOnly
                maxRows={24}
                value={JSON.stringify(
                  {
                    ...scenario,
                    random: undefined,
                    invariants: undefined,
                    clients: undefined,
                    replicas: undefined,
                    faults: undefined,
                    availableActions: undefined,
                    transport: scenario?.transport
                      ? { ...scenario.transport, queuedMessages: undefined }
                      : undefined,
                  },
                  null,
                  2,
                )}
              />
            </Card.Section>
          </Card>
        </Tabs.Panel>

        <Tabs.Panel value="actions" pt="md">
          <Stack gap="md">
            <Group>
              <DoSchedulerActionIcon
                scenarioId={scenarioId}
                onChange={setSelectedStrategy}
              />
            </Group>
            <SimpleGrid cols={{ base: 1, md: 2 }} spacing="md">
              <Box>
                <Title order={4}>Strategy</Title>
                <StrategyActionList
                  scenarioId={scenarioId}
                  strategyId={selectedStrategy ?? ""}
                />
              </Box>
              <Box>
                <Title order={4}>Metadata</Title>
                <SchedulerScenarioMetadata
                  schedulerId={selectedStrategy ?? ""}
                  scenarioId={scenarioId}
                />
              </Box>
            </SimpleGrid>
            <Box>
              <Title order={4}>Enabled Scenario Actions</Title>
              <ScenarioActionList scenarioId={scenarioId} />
            </Box>
          </Stack>
        </Tabs.Panel>

        <Tabs.Panel value="nodes" pt="md">
          <NodeList scenarioId={scenarioId} />
        </Tabs.Panel>

        <Tabs.Panel value="faults" pt="md">
          <ScenarioFaultsList scenarioId={scenarioId} />
        </Tabs.Panel>

        <Tabs.Panel value="dropped" pt="md">
          <DroppedMessagesList scenarioId={scenarioId} />
        </Tabs.Panel>

        <Tabs.Panel value="schedule" pt="md">
          {scheduleResp?.data ? (
            <ScheduleDetails
              title="Current Schedule"
              schedule={scheduleResp.data}
              hideParameters
            />
          ) : (
            <Card withBorder>
              <Card.Section p="md">No schedule yet.</Card.Section>
            </Card>
          )}
        </Tabs.Panel>
      </Tabs>
    </Stack>
  );
};
