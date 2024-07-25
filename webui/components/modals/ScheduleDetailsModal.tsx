"use client";

import { ScheduleDetails } from "@/components/ScheduleDetails";
import { Schedule } from "@/hooks/useSavedSchedules";
import { type ContextModalProps } from "@mantine/modals";
import React from "react";

export function ScheduleDetailsModal({
  innerProps,
}: ContextModalProps<{ schedule: Schedule }>) {
  const schedule = innerProps.schedule;

  return <ScheduleDetails schedule={schedule} />;
}
