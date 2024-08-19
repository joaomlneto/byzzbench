"use client";

import { useScheduleNext } from "@/lib/byzzbench-client";
import { ActionIcon, Tooltip } from "@mantine/core";
import { IconRefresh } from "@tabler/icons-react";
import { useQueryClient } from "@tanstack/react-query";
import React from "react";

export const ForceSyncActionIcon = () => {
  const queryClient = useQueryClient();
  const { mutate } = useScheduleNext();

  return (
    <Tooltip label="Force resync">
      <ActionIcon
        onClick={(e) => {
          e.preventDefault();
          queryClient.invalidateQueries();
        }}
      >
        <IconRefresh />
      </ActionIcon>
    </Tooltip>
  );
};
