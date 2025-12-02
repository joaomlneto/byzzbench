import { SimulationAccordion } from "@/components/Simulation";
import { Container, Stack, Title } from "@mantine/core";
import React from "react";

// This is a Server Component (no 'use client'), which can render Client Components below.
// Next.js (App Router) passes route params as a Promise in the latest typings.
export default async function Page({
  params,
}: {
  params: Promise<{ slug: string }>;
}) {
  const { slug } = await params;
  const scenarioIdStr = slug ?? "";
  const scenarioIdNum = Number(scenarioIdStr);

  if (!scenarioIdStr) {
    return <Container p="xl">Loading scenario...</Container>;
  }

  if (Number.isNaN(scenarioIdNum)) {
    return <Container p="xl">Invalid scenario ID: {scenarioIdStr}</Container>;
  }

  return (
    <Container p="xl">
      <Title order={1}>Scenario {scenarioIdNum}</Title>
      <Stack gap="md">
        <SimulationAccordion scenarioId={scenarioIdNum} />
      </Stack>
    </Container>
  );
}
