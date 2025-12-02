"use client";

import { SchedulerAnchor } from "@/components/Anchor";
import { useGetSchedulers } from "@/lib/byzzbench-client";
import { Container, Title } from "@mantine/core";
import React from "react";

export default function Page() {
  const { data: schedulerIds } = useGetSchedulers();

  return (
    <Container p="xl">
      <Title order={3}>Exploration Strategies</Title>
      <ul>
        {schedulerIds?.data.map((schedulerId) => (
          <li key={schedulerId}>
            <SchedulerAnchor schedulerId={schedulerId}>
              {schedulerId}
            </SchedulerAnchor>
          </li>
        ))}
      </ul>
    </Container>
  );
}
