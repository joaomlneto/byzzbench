"use client";

import { NodeCard } from "@/components/NodeCard";
import { useGetScenarioClients } from "@/lib/byzzbench-client";
import { Grid } from "@mantine/core";
import React from "react";

export type ClientListProps = {
  scenarioId: number;
};

export const ClientList = ({ scenarioId }: ClientListProps) => {
  const { data: clientIds } = useGetScenarioClients(scenarioId, {
    query: { retry: true },
  });
  return (
    <Grid gutter="md">
      {clientIds?.data.map((clientId) => (
        <Grid.Col span="auto" key={clientId}>
          <NodeCard scenarioId={scenarioId} nodeId={clientId} />
        </Grid.Col>
      ))}
    </Grid>
  );
};
