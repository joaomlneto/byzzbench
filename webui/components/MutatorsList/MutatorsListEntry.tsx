"use client";

import { useGetMutator } from "@/lib/byzzbench-client/generated";
import { Container, Grid, Text, Title } from "@mantine/core";
import React from "react";

export const MutatorsListEntry = ({ mutatorId }: { mutatorId: string }) => {
  const { data: mutator } = useGetMutator(mutatorId);

  if (!mutator) {
    return "Loading...";
  }

  return (
    <Container fluid style={{ border: "1px solid black" }} p="md">
      <Grid justify="space-between">
        <Grid.Col span="content">
          <Text size="sm">{mutator.data.id}</Text>
          <Title order={6}>{mutator.data.name}</Title>
          <Text size="xs">
            {// @ts-ignore
            mutator.data["inputClasses"]
              //?.map((s) => s.split(".").at(-1))
              ?.join(", ")}
          </Text>
        </Grid.Col>
      </Grid>
    </Container>
  );
};
