"use client";

import { ScheduleDetails } from "@/components/Schedule";
import { Schedule } from "@/lib/byzzbench-client";
import { type ContextModalProps } from "@mantine/modals";
import React from "react";

export function ScheduleDetailsModal({
  innerProps,
}: ContextModalProps<{ title: string; schedule: Schedule }>) {
  return (
    <ScheduleDetails
      title={innerProps.title}
      schedule={innerProps.schedule}
      hideDetailsButton
    />
  );
}
