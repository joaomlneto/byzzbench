"use client";

import { ServerScheduleDetails } from "@/components/ServerScheduleDetails";
import { useGetNumSavedSchedules } from "@/lib/byzzbench-client";
import { Container, Pagination, Title } from "@mantine/core";
import { usePagination } from "@mantine/hooks";
import React, { useMemo } from "react";

const ITEMS_PER_PAGE = 5;

export default function Home() {
  const numSavedSchedulesQuery = useGetNumSavedSchedules();

  const numPages = useMemo(
    () =>
      Math.ceil(
        (numSavedSchedulesQuery.data?.data.length ?? 0) / ITEMS_PER_PAGE,
      ),
    [numSavedSchedulesQuery.data?.data.length],
  );

  const pagination = usePagination({
    total: numPages,
  });

  const start = (pagination.active - 1) * ITEMS_PER_PAGE;
  const end = start + ITEMS_PER_PAGE;

  return (
    <Container fluid p="xl">
      <Title order={1}>Saved Schedules on the server</Title>
      <Pagination
        total={numPages}
        onChange={pagination.setPage}
        siblings={3}
        boundaries={2}
      />
      {numSavedSchedulesQuery.data?.data.slice(start, end).map((scheduleId) => (
        <div key={scheduleId}>
          <ServerScheduleDetails scheduleId={scheduleId} />
        </div>
      ))}
    </Container>
  );
}
