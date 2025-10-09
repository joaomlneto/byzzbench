"use client";

import { useGetScenarioNodes } from "@/lib/byzzbench-client";
import { Grid, Loader } from "@mantine/core";
import { useLocalStorage } from "@mantine/hooks";
import React from "react";
import { NodeCard } from "./NodeCard";

export type NodeListProps = {
  scenarioId: number;
};

export const NodeList = ({ scenarioId }: NodeListProps) => {
  const { data: nodeIds, isLoading } = useGetScenarioNodes(scenarioId, {
    query: { retry: true },
  });

  const [showMailboxes, setShowMailboxes] = useLocalStorage<boolean>({
    key: "byzzbench/showMailboxes",
    defaultValue: false,
  });

  if (isLoading) {
    return <Loader />;
  }

  return (
    <Grid gutter="md">
      {nodeIds?.data.map((nodeId) => (
        <Grid.Col span="content" key={nodeId}>
          <NodeCard
            scenarioId={scenarioId}
            nodeId={nodeId}
            showMailboxes={showMailboxes}
          />
        </Grid.Col>
      ))}
    </Grid>
  );
};
