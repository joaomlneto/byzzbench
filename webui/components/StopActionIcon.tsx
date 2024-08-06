"use client";

import { useStop } from "@/lib/byzzbench-client";
import { ActionIcon, Tooltip } from "@mantine/core";
import { showNotification } from "@mantine/notifications";
import { IconPlayerStopFilled } from "@tabler/icons-react";
import { useQueryClient } from "@tanstack/react-query";
import React from "react";

export const StopActionIcon = () => {
  const queryClient = useQueryClient();
  const { mutate } = useStop();

  return (
    <Tooltip label="Stop running scenarios in the background">
      <ActionIcon
        onClick={async (e) => {
          e.preventDefault();
          await mutate(null as never, {
            onSuccess: () => {
              queryClient.invalidateQueries();
              showNotification({
                message: "Stopped background scenarios",
              });
            },
            onError: () => {
              showNotification({
                message: "Failed to stop background scenarios",
                color: "red",
              });
            },
          });
        }}
      >
        <IconPlayerStopFilled />
      </ActionIcon>
    </Tooltip>
  );
};
