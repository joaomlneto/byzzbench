import { ScenarioAside } from "@/components/Scenario";
import { SimulationAccordion } from "@/components/Simulation";
import { Container, Stack, Title } from "@mantine/core";
import React from "react";

// This is a Server Component (no 'use client'), which can render Client Components below.
export default function Page({
  params,
}: {
  params: { slug: string };
}) {
  const scenarioIdStr = params?.slug ?? "";
  const scenarioIdNum = Number(scenarioIdStr);

  if (!scenarioIdStr) {
    return <Container p="xl">Loading scenario...</Container>;
  }

  if (Number.isNaN(scenarioIdNum)) {
    return (
      <Container p="xl">Invalid scenario ID: {scenarioIdStr}</Container>
    );
  }

  return (
    <Container p="xl">
      <Title order={1}>Scenario {scenarioIdNum}</Title>
      <Stack gap="md">
        <SimulationAccordion scenarioId={scenarioIdNum} />
      </Stack>
      <ScenarioAside scenarioId={scenarioIdNum} />
    </Container>
  );
}
