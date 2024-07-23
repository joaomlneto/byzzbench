"use client";

import { Schedule } from "@/hooks/useSavedSchedules";
import { Title } from "@mantine/core";
import React from "react";

export type ScheduleDetailsProps = {
  schedule: Schedule;
};

export const ScheduleDetails = ({ schedule }: ScheduleDetailsProps) => {
  return (
    <div>
      <Title order={4}>{schedule.name}</Title>
      {schedule.actions.map((action, i) => (
        <div key={i}>
          {action.type == "DeliverEvent" &&
            `Deliver event #${action.event.eventId}`}
        </div>
      ))}
    </div>
  );
};
