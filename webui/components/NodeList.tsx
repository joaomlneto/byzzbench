"use client";

import { useGetNodes } from "@/lib/byzzbench-client";
import { Grid, Loader } from "@mantine/core";
import React from "react";
import { NodeCard } from "./NodeCard";

export const NodeList = () => {
  const { data: nodeIds, isLoading } = useGetNodes({ query: { retry: true } });

  if (isLoading) {
    return <Loader />;
  }

  return (
    <Grid gutter="md">
      {nodeIds?.data.map((nodeId) => (
        <Grid.Col span="content" key={nodeId}>
          <NodeCard nodeId={nodeId} />
        </Grid.Col>
      ))}
    </Grid>
  );
};
