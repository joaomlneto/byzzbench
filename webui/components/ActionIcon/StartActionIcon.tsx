"use client";

import { start } from "@/lib/byzzbench-client";
import { ActionIcon, Tooltip } from "@mantine/core";
import { showNotification } from "@mantine/notifications";
import { IconPlayerTrackNextFilled } from "@tabler/icons-react";
import { useQueryClient } from "@tanstack/react-query";
import React from "react";

export type StartActionIconProps = {
  campaignId: number;
};

export const StartActionIcon = ({ campaignId }: StartActionIconProps) => {
  const queryClient = useQueryClient();

  return (
    <Tooltip label="Start running scenarios in the background">
      <ActionIcon
        onClick={async (e) => {
          e.preventDefault();
          await start(campaignId, {}).then(() => {
            queryClient.invalidateQueries();
            showNotification({
              message: "Running scenarios in the background",
            });
          });
        }}
      >
        <IconPlayerTrackNextFilled />
      </ActionIcon>
    </Tooltip>
  );
};
