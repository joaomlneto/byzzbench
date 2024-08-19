"use client";

import { ScheduleDetails, ScheduleDetailsProps } from "@/components/Schedule";
import { useGetSavedSchedule } from "@/lib/byzzbench-client";
import React, { memo } from "react";

export type ServerScheduleDetailsProps = Omit<
  ScheduleDetailsProps,
  "schedule" | "title"
> & {
  title?: string;
  scheduleId: number;
};

export const ServerScheduleDetails = memo(
  ({ scheduleId, title, ...otherProps }: ServerScheduleDetailsProps) => {
    const savedScheduleQuery = useGetSavedSchedule(scheduleId);
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
