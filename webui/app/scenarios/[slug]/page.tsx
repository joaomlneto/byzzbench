import { ScenarioDebugger } from "@/components/Scenario/ScenarioDebugger";
import { Container, Title } from "@mantine/core";
import React from "react";

// Server Component entry for the new scenario debugger page
export default function Page({ params }: { params: { slug: string } }) {
  const scenarioIdStr = params?.slug ?? "";
  const scenarioIdNum = Number(scenarioIdStr);

  if (!scenarioIdStr) {
    return <Container p="xl">Loading scenario...</Container>;
  }

  if (Number.isNaN(scenarioIdNum)) {
    return <Container p="xl">Invalid scenario ID: {scenarioIdStr}</Container>;
  }

  return (
    <Container fluid p="md">
      <Title order={1} mb="md">
        Scenario {scenarioIdNum}
      </Title>
      <ScenarioDebugger scenarioId={scenarioIdNum} />
    </Container>
  );
}
