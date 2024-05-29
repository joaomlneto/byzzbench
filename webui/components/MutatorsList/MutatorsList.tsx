"use client";

import { MutatorsListEntry } from "@/components/MutatorsList/MutatorsListEntry";
import { useGetMutators } from "@/lib/byzzbench-client";
import { Grid } from "@mantine/core";
import React from "react";

export const MutatorsList = () => {
  const { data: nodeIds } = useGetMutators({ query: { retry: true } });
  return (
    <Grid gutter="md">
      {nodeIds?.data.map((nodeId) => (
        <Grid.Col span="auto" key={nodeId}>
          <MutatorsListEntry mutatorId={nodeId} />
        </Grid.Col>
      ))}
    </Grid>
  );
};
