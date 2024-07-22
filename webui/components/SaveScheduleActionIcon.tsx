"use client";

import { useScheduleNext } from "@/lib/byzzbench-client";
import { ActionIcon, Title, Tooltip } from "@mantine/core";
import { openContextModal } from "@mantine/modals";
import { IconDeviceFloppy } from "@tabler/icons-react";
import { useQueryClient } from "@tanstack/react-query";
import React from "react";

export const SaveScheduleActionIcon = () => {
  const queryClient = useQueryClient();
  const { mutate } = useScheduleNext();

  return (
    <Tooltip label="Save current schedule">
      <ActionIcon
        onClick={async (e) => {
          e.preventDefault();

          openContextModal({
            modal: "saveSchedule",
            title: <Title order={3}>Save Schedule</Title>,
            size: "xl",
            centered: true,
            innerProps: {
              actions: [],
            },
          });
        }}
      >
        <IconDeviceFloppy />
      </ActionIcon>
    </Tooltip>
  );
};
