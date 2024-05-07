"use client";

import { useGetNodes } from "@/lib/bftbench-client";
import { Grid } from "@mantine/core";
import React from "react";
import { NodeCard } from "./NodeCard";

export const NodeList = () => {
  const { data: nodeIds } = useGetNodes({ query: { retry: true } });
  return (
    <Grid gutter="md">
      {nodeIds?.data.map((nodeId) => (
        <Grid.Col span="auto" key={nodeId}>
          <NodeCard nodeId={nodeId} />
        </Grid.Col>
      ))}
    </Grid>
  );
};
