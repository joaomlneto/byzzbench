"use client";

import { useStart } from "@/lib/byzzbench-client";
import { ActionIcon, Tooltip } from "@mantine/core";
import { showNotification } from "@mantine/notifications";
import { IconPlayerTrackNextFilled } from "@tabler/icons-react";
import { useQueryClient } from "@tanstack/react-query";
import React from "react";

export const StartActionIcon = () => {
  const queryClient = useQueryClient();
  const { mutate } = useStart({
    eventsPerRun: 10,
  });

  return (
    <Tooltip label="Start running scenarios in the background">
      <ActionIcon
        onClick={async (e) => {
          e.preventDefault();
          await mutate(null as never, {
            onSuccess: () => {
              queryClient.invalidateQueries();
              showNotification({
                message: "Running scenarios in the background",
              });
            },
          });
        }}
      >
        <IconPlayerTrackNextFilled />
      </ActionIcon>
    </Tooltip>
  );
};
