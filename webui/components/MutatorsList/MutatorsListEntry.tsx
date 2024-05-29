"use client";

import { useGetMutator } from "@/lib/byzzbench-client/generated";
import { Container, Group, Text, Title } from "@mantine/core";
import React from "react";

export const MutatorsListEntry = ({ mutatorId }: { mutatorId: number }) => {
  const { data: mutator } = useGetMutator(mutatorId);

  if (!mutator) {
    return "Loading...";
  }

  return (
    <Container fluid style={{ border: "1px solid black" }} p="md">
      <Group justify="space-between" wrap="nowrap" mb="sm">
        <div>
          <Title order={4}>
            {mutatorId}: {mutator.data.name}
          </Title>
          <Text lineClamp={1}>
            {mutator.data.inputClasses
              ?.map((s) => s.split(".").at(-1))
              .join(", ")}
          </Text>
        </div>
      </Group>
    </Container>
  );
};
