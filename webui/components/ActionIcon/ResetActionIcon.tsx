"use client";

import { ActionIcon, Tooltip } from "@mantine/core";
import { showNotification } from "@mantine/notifications";
import { IconTrash } from "@tabler/icons-react";
import React from "react";

export const ResetActionIcon = () => {
  return (
    <Tooltip label="Reset simulation">
      <ActionIcon
        onClick={(e) => {
          e.preventDefault();
          showNotification({
            message: "No longer available",
          });
          /*
          mutate(null as never, {
            onSuccess: () => {
              showNotification({
                message: `Simulation reset`,
              });
            },
            onError: () => {
              showNotification({
                message: "Failed to reset simulation",
                color: "red",
              });
            },
            onSettled: async () => {
              await queryClient.invalidateQueries();
            },
          });*/
        }}
      >
        <IconTrash />
      </ActionIcon>
    </Tooltip>
  );
};
