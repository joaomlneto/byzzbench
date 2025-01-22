"use client";

import { useGetReplicas } from "@/lib/byzzbench-client";
import { Grid, Loader } from "@mantine/core";
import React from "react";
import { NodeCard } from "./NodeCard";

export type NodeListProps = {
  showMailboxes?: boolean;
};

export const NodeList = ({ showMailboxes = true }: NodeListProps) => {
  const { data: nodeIds, isLoading } = useGetReplicas({
    query: { retry: true },
  });

  if (isLoading) {
    return <Loader />;
  }

  return (
    <Grid gutter="md">
      {nodeIds?.data.map((nodeId) => (
        <Grid.Col span="content" key={nodeId}>
          <NodeCard nodeId={nodeId} showMailboxes={showMailboxes} />
        </Grid.Col>
      ))}
    </Grid>
  );
};
