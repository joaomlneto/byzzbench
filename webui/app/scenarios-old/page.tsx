"use client";

import { useGetScenarios } from "@/lib/byzzbench-client";
import { Anchor, Container, SimpleGrid, Title } from "@mantine/core";
import Link from "next/link";
import React from "react";

/*
const AdoBStateDiagram = dynamic<{}>(
    () =>
        import("@/components/adob/AdoBStateDiagram").then(
            (m) => m.AdoBStateDiagram,
        ),
    {
        ssr: false,
    },
);*/

export default function Home() {
  const { data: scenarioIds } = useGetScenarios();

  return (
    <Container fluid p="xl">
      <SimpleGrid cols={2}>
        <div>
          <Title order={3}>Active Scenarios</Title>
          <ul>
            {scenarioIds?.data.map((scenarioId) => (
              <li key={scenarioId}>
                <Anchor component={Link} href={`/scenarios/${scenarioId}`}>
                  Scenario {scenarioId}
                </Anchor>
              </li>
            ))}
          </ul>
        </div>
      </SimpleGrid>
    </Container>
  );
}
