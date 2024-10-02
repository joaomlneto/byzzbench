"use client";

import { useReset } from "@/lib/byzzbench-client";
import { ActionIcon, Tooltip } from "@mantine/core";
import { showNotification } from "@mantine/notifications";
import { IconTrash } from "@tabler/icons-react";
import { useQueryClient } from "@tanstack/react-query";
import React from "react";

export const ResetActionIcon = () => {
  const queryClient = useQueryClient();
  const { mutate } = useReset();

  return (
    <Tooltip label="Reset simulation">
      <ActionIcon
        onClick={(e) => {
          e.preventDefault();
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
          });
        }}
      >
        <IconTrash />
      </ActionIcon>
    </Tooltip>
  );
};
