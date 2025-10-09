import { SchedulerState } from "@/components/Scheduler/SchedulerState";
import { Container, Title } from "@mantine/core";
import React from "react";

export default async function Page({
  params,
}: {
  params: Promise<{ slug: string }>;
}) {
  const { slug: schedulerId } = await params;

  if (!schedulerId) {
    return "loading data";
  }

  return (
    <Container p="xl">
      <Title order={1}>{schedulerId}</Title>
      <SchedulerState schedulerId={schedulerId} />
    </Container>
  );
}
