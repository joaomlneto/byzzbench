"use client";

import { useScheduleNext } from "@/lib/byzzbench-client";
import { ActionIcon, Tooltip } from "@mantine/core";
import { showNotification } from "@mantine/notifications";
import { IconPlayerPlay } from "@tabler/icons-react";
import { useQueryClient } from "@tanstack/react-query";
import React from "react";

export const DoSchedulerActionIcon = () => {
  const queryClient = useQueryClient();
  const { mutate } = useScheduleNext();

  return (
    <Tooltip label="Perform next scheduler action">
      <ActionIcon
        onClick={(e) => {
          e.preventDefault();
          mutate(null as never, {
            onSuccess: () => {
              // do nothing
            },
            onError: () => {
              showNotification({
                message: "Failed to schedule next action",
                color: "red",
              });
            },
            onSettled: async () => {
              await queryClient.invalidateQueries();
            },
          });
        }}
      >
        <IconPlayerPlay />
      </ActionIcon>
    </Tooltip>
  );
};
