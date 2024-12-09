"use client";

import { NodeCard } from "@/components/NodeCard";
import { useGetClients } from "@/lib/byzzbench-client";
import { Grid } from "@mantine/core";
import React from "react";

export const ClientList = () => {
  const { data: clientIds } = useGetClients({ query: { retry: true } });
  return (
    <Grid gutter="md">
      {clientIds?.data.map((clientId) => (
        <Grid.Col span="auto" key={clientId}>
          <NodeCard nodeId={clientId} />
        </Grid.Col>
      ))}
    </Grid>
  );
};
