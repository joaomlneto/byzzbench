"use client";

import { useGetNodes } from "@/lib/bftbench-client";
import { Grid } from "@mantine/core";
import React from "react";
import { NodeCard } from "./NodeCard";

export const NodeList = () => {
  const { data } = useGetNodes({ query: { refetchInterval: 1000 } });
  return (
    <Grid gutter="md">
      {Object.entries(data?.data ?? {})
        .filter(([id, node]) => id !== "__className__")
        .map(([id, node]) => (
          <Grid.Col span="auto" key={id}>
            <NodeCard nodeId={node} />
          </Grid.Col>
        ))}
    </Grid>
  );
};
