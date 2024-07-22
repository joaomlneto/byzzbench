"use client";

import { useGetClients } from "@/lib/byzzbench-client";
import { Grid } from "@mantine/core";
import React from "react";
import { ClientCard } from "./ClientCard";

export const ClientList = () => {
  const { data: clientIds } = useGetClients({ query: { retry: true } });
  return (
    <Grid gutter="md">
      {clientIds?.data.map((clientId) => (
        <Grid.Col span="auto" key={clientId}>
          <ClientCard clientId={clientId} />
        </Grid.Col>
      ))}
    </Grid>
  );
};
