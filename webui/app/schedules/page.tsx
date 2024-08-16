"use client";

import { ServerScheduleDetails } from "@/components/ServerScheduleDetails";
import { useGetNumSavedSchedules } from "@/lib/byzzbench-client";
import { Container, Title } from "@mantine/core";
import React from "react";

export default function Home() {
  const numSavedSchedulesQuery = useGetNumSavedSchedules();

  return (
    <Container fluid p="xl">
      <Title order={1}>Saved Schedules on the server (first 20)</Title>
      {numSavedSchedulesQuery.data?.data.slice(0, 20).map((scheduleId) => (
        <p key={scheduleId}>
          <ServerScheduleDetails scheduleId={scheduleId} />
        </p>
      ))}
    </Container>
  );
}
