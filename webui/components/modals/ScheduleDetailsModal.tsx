"use client";

import { ScheduleDetails } from "@/components/Schedule";
import { Schedule } from "@/lib/byzzbench-client";
import { type ContextModalProps } from "@mantine/modals";
import React from "react";

export function ScheduleDetailsModal({
  innerProps,
}: ContextModalProps<{ schedule: Schedule }>) {
  const schedule = innerProps.schedule;

  return <ScheduleDetails schedule={schedule} />;
}
