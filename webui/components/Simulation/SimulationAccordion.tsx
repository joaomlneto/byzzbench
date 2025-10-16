"use client";

import { ScenarioActionList, StrategyActionList } from "@/components/Action";
import { DoSchedulerActionIcon } from "@/components/ActionIcon";
import { ScenarioFaultsList } from "@/components/FaultsList";
import { NodeList } from "@/components/NodeList";
import { PredicateList } from "@/components/PredicateList";
import { ShowMailboxesSwitch } from "@/components/Scenario";
import { SchedulerScenarioMetadata } from "@/components/Scheduler";
import { useGetScenario } from "@/lib/byzzbench-client";
import {
  Accordion,
  AccordionProps,
  Anchor,
  Group,
  JsonInput,
  SimpleGrid,
} from "@mantine/core";
import { useLocalStorage } from "@mantine/hooks";
import Link from "next/link";
import React from "react";

export type SimulationAccordionProps = AccordionProps & {
  scenarioId: number;
};

export const SimulationAccordion = ({
  scenarioId,
}: SimulationAccordionProps) => {
  const [selectedStrategy, setSelectedStrategy] = React.useState<string | null>(
    null,
  );
  const [selectedAccordionEntries, setSelectedAccordionEntries] =
    useLocalStorage<string[]>({
      key: "byzzbench/selectedAccordionEntries",
      defaultValue: ["nodes", "schedule"],
    });

  const { data } = useGetScenario(scenarioId);

  return (
    <Accordion
      multiple
      variant="separated"
      value={selectedAccordionEntries}
      onChange={setSelectedAccordionEntries}
    >
      <Group wrap="nowrap" gap="xs" align="center">
        <p>{data?.data.description}</p>
        <PredicateList scenarioId={scenarioId} />
        <ShowMailboxesSwitch />
        {data?.data?.scheduleId && (
          <Anchor component={Link} href={`/schedules/${data.data.scheduleId}`}>
            {`Schedule ${data.data.scheduleId}`}
          </Anchor>
        )}
        {data?.data?.campaignId && (
          <Anchor
            component={Link}
            href={`/campaigns/${data?.data?.campaignId}`}
          >
            Campaign {data.data.campaignId}
          </Anchor>
        )}
      </Group>
      <Accordion.Item key="parameters" value="parameters">
        <Accordion.Control>Scenario Info</Accordion.Control>
        <Accordion.Panel>
          <JsonInput
            label="Scenario"
            variant="unstyled"
            inputSize="xs"
            size="xs"
            autosize
            readOnly
            maxRows={20}
            value={JSON.stringify(
              {
                ...data?.data,
                random: undefined,
                invariants: undefined,
                clients: undefined,
                replicas: undefined,
                faults: undefined,
                availableActions: undefined,
              },
              null,
              2,
            )}
          />
        </Accordion.Panel>
      </Accordion.Item>
      <Accordion.Item key="strategy-actions" value="strategy-actions">
        <Accordion.Control>Exploration Strategy</Accordion.Control>
        <Accordion.Panel>
          <DoSchedulerActionIcon
            scenarioId={scenarioId}
            onChange={setSelectedStrategy}
          />
          <SimpleGrid cols={2}>
            <StrategyActionList
              scenarioId={scenarioId}
              strategyId={selectedStrategy ?? ""}
            />
            <SchedulerScenarioMetadata
              schedulerId={selectedStrategy ?? ""}
              scenarioId={scenarioId}
            />
          </SimpleGrid>
        </Accordion.Panel>
      </Accordion.Item>
      <Accordion.Item key="actions" value="actions">
        <Accordion.Control>Enabled Scenario Actions</Accordion.Control>
        <Accordion.Panel>
          <ScenarioActionList scenarioId={scenarioId} />
        </Accordion.Panel>
      </Accordion.Item>
      <Accordion.Item key="faults" value="faults">
        <Accordion.Control>Faults</Accordion.Control>
        <Accordion.Panel>
          <ScenarioFaultsList scenarioId={scenarioId} />
        </Accordion.Panel>
      </Accordion.Item>
      <Accordion.Item key="nodes" value="nodes">
        <Accordion.Control>Nodes</Accordion.Control>
        <Accordion.Panel>
          <NodeList scenarioId={scenarioId} />
        </Accordion.Panel>
      </Accordion.Item>
      {/*<Accordion.Item key="adob" value="adob">
            <Accordion.Control>AdoB State</Accordion.Control>
            <Accordion.Panel>
              <AdoBStateDiagram />
            </Accordion.Panel>
          </Accordion.Item>*/}
    </Accordion>
  );
};
