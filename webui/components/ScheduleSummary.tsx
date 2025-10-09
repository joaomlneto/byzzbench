"use client";

import { ScheduleDetails } from "@/components/Schedule";
import { useGetSchedule } from "@/lib/byzzbench-client";

import { Badge } from "@mantine/core";
import React from "react";

export type ScheduleSummaryProps = {
  scheduleId: number;
};

export const ScheduleSummary = ({ scheduleId }: ScheduleSummaryProps) => {
  const { data } = useGetSchedule(scheduleId);

  if (!data) {
    return <p>Loading schedule data...</p>;
  }

  return (
    <>
      <p>
        Broken invariants ({data.data.brokenInvariants.length}):{" "}
        {data.data.brokenInvariants.map((invariant) => (
          <Badge key={invariant.id}>{invariant.id}</Badge>
        ))}
      </p>
      <p>Length: {data.data.actions.length}</p>
      <ScheduleDetails
        hideTitle
        title={`Schedule ${scheduleId}`}
        schedule={data.data}
      />
    </>
  );
};
