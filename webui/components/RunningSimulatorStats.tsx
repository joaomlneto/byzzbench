"use client";

import { useGetAllScheduleIds } from "@/lib/byzzbench-client";
import { DonutChart } from "@mantine/charts";
import { Container } from "@mantine/core";
import { useMemo } from "react";

export const RunningSimulatorStats = () => {
  const allSchedulesQuery = useGetAllScheduleIds({
    query: { refetchInterval: 1000 },
  });

  const numSchedules = useMemo(() => {
    return (
      (allSchedulesQuery.data?.data.buggySchedules.length ?? 0) +
      (allSchedulesQuery.data?.data.correctSchedules.length ?? 0)
    );
  }, [
    allSchedulesQuery.data?.data.buggySchedules.length,
    allSchedulesQuery.data?.data.correctSchedules.length,
  ]);

  const data = useMemo(() => {
    return [
      {
        name: "Correct",
        value: allSchedulesQuery.data?.data.correctSchedules.length ?? 0,
        color: "green",
      },
      {
        name: "Buggy",
        value: allSchedulesQuery.data?.data.buggySchedules.length ?? 0,
        color: "red",
      },
    ];
  }, [
    allSchedulesQuery.data?.data.correctSchedules.length,
    allSchedulesQuery.data?.data.buggySchedules.length,
  ]);

  return (
    <Container>
      <h1>Running Simulations</h1>
      <p>Schedules saved: {numSchedules}</p>
      <p>
        Correct schedules:{" "}
        {allSchedulesQuery.data?.data.correctSchedules.length}
      </p>
      <p>
        Buggy schedules: {allSchedulesQuery.data?.data.buggySchedules.length}
      </p>
      <DonutChart data={data} withLabelsLine withLabels labelsType="value" />
    </Container>
  );
};
