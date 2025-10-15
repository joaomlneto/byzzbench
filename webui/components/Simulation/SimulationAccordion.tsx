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
      {data && (
        <p>
          {data.data.description} -{" "}
          <Anchor component={Link} href={`/schedules/${data.data.scheduleId}`}>
            {`Schedule ${data.data.scheduleId}`}
          </Anchor>
        </p>
      )}
      <Group wrap="nowrap" gap="xs" align="center">
        <DoSchedulerActionIcon
          scenarioId={scenarioId}
          onChange={setSelectedStrategy}
        />
        <PredicateList scenarioId={scenarioId} />
        <ShowMailboxesSwitch />
        {data?.data?.campaignId && (
          <Anchor
            component={Link}
            href={`/campaigns/${data?.data?.campaignId}`}
          >
            Campaign {data.data.campaignId}
          </Anchor>
        )}
      </Group>
      <Accordion.Item key="strategy-actions" value="strategy-actions">
        <Accordion.Control>Exploration Strategy Actions</Accordion.Control>
        <Accordion.Panel>
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
