"use client";

import { ScheduleDetails } from "@/components/Schedule";
import { useGetSavedSchedule } from "@/lib/byzzbench-client";
import { Title } from "@mantine/core";
import React, { memo } from "react";

export type ServerScheduleDetailsProps = {
  scheduleId: number;
};

export const ServerScheduleDetails = memo(
  ({ scheduleId }: ServerScheduleDetailsProps) => {
    const savedScheduleQuery = useGetSavedSchedule(scheduleId);
    return (
      <div>
        <Title order={6}>Schedule {scheduleId}</Title>
        {savedScheduleQuery.data && (
          <ScheduleDetails schedule={savedScheduleQuery.data?.data} />
        )}
      </div>
    );
  },
);
ServerScheduleDetails.displayName = "ServerScheduleDetails";
