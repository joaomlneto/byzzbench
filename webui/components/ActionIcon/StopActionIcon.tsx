"use client";

import { StartActionIconProps } from "@/components/ActionIcon/StartActionIcon";
import { stop } from "@/lib/byzzbench-client";
import { ActionIcon, Tooltip } from "@mantine/core";
import { showNotification } from "@mantine/notifications";
import { IconPlayerStopFilled } from "@tabler/icons-react";
import { useQueryClient } from "@tanstack/react-query";
import React from "react";

export type StopActionIconProps = {
  campaignId: number;
};

export const StopActionIcon = ({ campaignId }: StartActionIconProps) => {
  const queryClient = useQueryClient();

  return (
    <Tooltip label="Stop running scenarios in the background">
      <ActionIcon
        onClick={async (e) => {
          e.preventDefault();
          await stop(campaignId, {})
            .then(() => {
              queryClient.invalidateQueries();
              showNotification({
                message: "Stopped background scenarios",
              });
            })
            .catch(() => {
              showNotification({
                message: "Failed to stop background scenarios",
                color: "red",
              });
            });
        }}
      >
        <IconPlayerStopFilled />
      </ActionIcon>
    </Tooltip>
  );
};
