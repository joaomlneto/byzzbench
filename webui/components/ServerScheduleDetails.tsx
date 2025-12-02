"use client";

import { ScheduleDetails, ScheduleDetailsProps } from "@/components/Schedule";
import { useGetSchedule } from "@/lib/byzzbench-client";
import React, { memo } from "react";

export type ServerScheduleDetailsProps = Omit<
  ScheduleDetailsProps,
  "schedule" | "title"
> & {
  scheduleId: number;
  title?: string;
};

export const ServerScheduleDetails = memo(
  ({ scheduleId, title, ...otherProps }: ServerScheduleDetailsProps) => {
    const savedScheduleQuery = useGetSchedule(scheduleId);
    return (
      savedScheduleQuery.data && (
        <ScheduleDetails
          title={title ?? `Schedule ${scheduleId}`}
          schedule={savedScheduleQuery.data?.data}
          hideSchedule
          {...otherProps}
        />
      )
    );
  },
);
ServerScheduleDetails.displayName = "ServerScheduleDetails";
