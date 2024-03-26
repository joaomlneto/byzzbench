"use client";

import { JsonTable } from "@/components/JsonTable";
import { useGetNodes } from "@/lib/bftbench-client";
import { Container, Grid, Title } from "@mantine/core";
import React from "react";

export const NodeList = () => {
  const { data } = useGetNodes({ query: { refetchInterval: 1000 } });
  return (
    <Grid gutter="md">
      {Object.entries(data?.data ?? {})
        .filter(([id, node]) => id !== "__className__")
        .map(([id, node]) => (
          <Grid.Col span="auto" key={id}>
            <Container fluid style={{ border: "1px solid black" }} p="md">
              <Title order={4}>{id}</Title>
              <JsonTable data={node} />
            </Container>
          </Grid.Col>
        ))}
    </Grid>
  );
};
