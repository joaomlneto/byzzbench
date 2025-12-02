"use client";

import { useGetSchedule } from "@/lib/byzzbench-client";
import { ActionIcon, Title, Tooltip } from "@mantine/core";
import { openContextModal } from "@mantine/modals";
import { showNotification } from "@mantine/notifications";
import { IconDeviceFloppy } from "@tabler/icons-react";
import { useQueryClient } from "@tanstack/react-query";
import React from "react";

export type SaveScheduleActionIconProps = {
  scheduleId: number;
};

export const SaveScheduleActionIcon = ({
  scheduleId,
}: SaveScheduleActionIconProps) => {
  const queryClient = useQueryClient();

  const { data: schedule } = useGetSchedule(scheduleId);

  return (
    <Tooltip label="Save current schedule">
      <ActionIcon
        onClick={async (e) => {
          e.preventDefault();
          if (!schedule) {
            showNotification({
              message: "No schedule to save",
              color: "red",
            });
            return;
          }
          openContextModal({
            modal: "saveSchedule",
            title: <Title order={3}>Save Schedule</Title>,
            size: "xl",
            centered: true,
            innerProps: {
              schedule: schedule.data,
            },
          });
          queryClient.invalidateQueries();
        }}
      >
        <IconDeviceFloppy />
      </ActionIcon>
    </Tooltip>
  );
};
