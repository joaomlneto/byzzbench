"use client";

import { ServerScheduleDetails } from "@/components/ServerScheduleDetails";
import { useGetAllScheduleIds } from "@/lib/byzzbench-client";
import { Container, Pagination, Select, Title } from "@mantine/core";
import { usePagination } from "@mantine/hooks";
import React, { useMemo, useState } from "react";

const ITEMS_PER_PAGE = 20;

export default function Home() {
  const [selected, setSelected] = useState<"all" | "correct" | "buggy">("all");
  const scheduleIdsQuery = useGetAllScheduleIds({
    query: { enabled: selected === "all" },
  });

  const allScheduleIds = useMemo(
    () =>
      [
        ...(scheduleIdsQuery.data?.data.correctSchedules ?? []),
        ...(scheduleIdsQuery.data?.data.buggySchedules ?? []),
      ].toSorted((a, b) => a - b),
    [
      scheduleIdsQuery.data?.data.correctSchedules,
      scheduleIdsQuery.data?.data.buggySchedules,
    ],
  );

  const schedules = useMemo(() => {
    switch (selected) {
      case "all":
        return allScheduleIds;
      case "buggy":
        return scheduleIdsQuery.data?.data.buggySchedules ?? [];
      case "correct":
        return scheduleIdsQuery.data?.data.correctSchedules ?? [];
    }
  }, [
    selected,
    allScheduleIds,
    scheduleIdsQuery.data?.data.buggySchedules,
    scheduleIdsQuery.data?.data.correctSchedules,
  ]);

  const numPages = useMemo(
    () => Math.ceil(schedules.length) / ITEMS_PER_PAGE,
    [schedules.length],
  );

  const pagination = usePagination({
    total: numPages,
  });

  const start = (pagination.active - 1) * ITEMS_PER_PAGE;
  const end = start + ITEMS_PER_PAGE;

  return (
    <Container>
      <Title order={1}>Saved Schedules on the server</Title>
      <Select
        size="xs"
        value={selected}
        onChange={(value) => setSelected(value as "all" | "correct" | "buggy")}
        data={[
          { value: "all", label: `All (${allScheduleIds.length})` },
          {
            value: "correct",
            label: `Correct (${scheduleIdsQuery.data?.data.correctSchedules.length})`,
          },
          {
            value: "buggy",
            label: `Buggy (${scheduleIdsQuery.data?.data.buggySchedules.length})`,
          },
        ]}
        label="Filter by"
        maw={150}
      />
      <Pagination
        size="sm"
        total={numPages}
        onChange={pagination.setPage}
        siblings={3}
        boundaries={2}
      />
      {schedules.slice(start, end).map((scheduleId) => (
        <div key={scheduleId}>
          <ServerScheduleDetails scheduleId={scheduleId} />
        </div>
      ))}
    </Container>
  );
}
