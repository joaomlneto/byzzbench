import { ScenarioAside } from "@/components/Scenario";
import { SimulationAccordion } from "@/components/Simulation";
import { Container, Stack, Title } from "@mantine/core";
import React from "react";

export default async function Page({
  params,
}: {
  params: Promise<{ slug: string }>;
}) {
  const { slug: scenarioId } = await params;

  if (!scenarioId) {
    return "loading data";
  }

  if (isNaN(Number(scenarioId))) {
    return <Container p="xl">Invalid scenario ID: {scenarioId}</Container>;
  }

  return (
    <Container p="xl">
      <Title order={1}>Scenario {scenarioId}</Title>
      <Stack gap="md">
        <SimulationAccordion scenarioId={Number(scenarioId)} />
      </Stack>
      <ScenarioAside scenarioId={Number(scenarioId)} />
    </Container>
  );
}
