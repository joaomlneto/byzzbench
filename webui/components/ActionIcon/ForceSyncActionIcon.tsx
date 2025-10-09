"use client";

import { ActionIcon, Tooltip } from "@mantine/core";
import { IconRefresh } from "@tabler/icons-react";
import { useQueryClient } from "@tanstack/react-query";
import React from "react";

export type ForceSyncActionIconProps = {};

export const ForceSyncActionIcon = ({}: ForceSyncActionIconProps) => {
  const queryClient = useQueryClient();

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
