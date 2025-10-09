"use client";

import { useGetScenario } from "@/lib/byzzbench-client";
import { CardProps, Container, JsonInput } from "@mantine/core";
import React from "react";

export type ScenarioCardProps = {
  scenarioId: number;
} & CardProps;

export const ScenarioCard = ({
  scenarioId,
  ...otherProps
}: ScenarioCardProps) => {
  const { data } = useGetScenario(scenarioId);

  return (
    <Container size="xl">
      <JsonInput value={JSON.stringify(data?.data, null, 2)} autosize />
    </Container>
  );
};
