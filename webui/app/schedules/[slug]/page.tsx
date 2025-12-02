import { ScheduleCard } from "@/components/Schedule";
import { ServerScheduleDetails } from "@/components/ServerScheduleDetails";
import { Container, Title } from "@mantine/core";
import React from "react";

export default async function Page({
  params,
}: {
  params: Promise<{ slug: string }>;
}) {
  const { slug: scheduleId } = await params;

  if (!scheduleId) {
    return "loading data";
  }

  if (isNaN(Number(scheduleId))) {
    return <Container p="xl">Invalid schedule ID: {scheduleId}</Container>;
  }

  return (
    <Container p="xl">
      <Title order={1}>Schedule {scheduleId}</Title>
      <ScheduleCard scheduleId={Number(scheduleId)} />
      <ServerScheduleDetails
        scheduleId={Number(scheduleId)}
        hideTitle
        hideScenario
        hideSchedule={false}
      />
    </Container>
  );
}
