"use client";

import { useGetSavedSchedule } from "@/lib/byzzbench-client";
import { JsonInput, Spoiler } from "@mantine/core";
import React from "react";

export type ServerScheduleDetailsProps = {
  scheduleId: number;
};

export const ServerScheduleDetails = ({
  scheduleId,
}: ServerScheduleDetailsProps) => {
  const savedScheduleQuery = useGetSavedSchedule(scheduleId);
  return (
    <div>
      Schedule {scheduleId} event IDs:{" "}
      {savedScheduleQuery.data?.data.events
        ?.map((event) => event.eventId)
        .join(", ")}
      <Spoiler maxHeight={0} showLabel="Show" hideLabel="Hide">
        <JsonInput
          label={`Schedule ${scheduleId}`}
          value={JSON.stringify(savedScheduleQuery.data?.data, null, 2)}
          autosize
          maxRows={30}
        />
      </Spoiler>
    </div>
  );
};
