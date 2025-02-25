"use client";

import { useGetConfig } from "@/lib/byzzbench-client";
import { ActionIcon, Tooltip } from "@mantine/core";
import { openContextModal } from "@mantine/modals";
import { IconFileSettings } from "@tabler/icons-react";
import { useQueryClient } from "@tanstack/react-query";
import React from "react";

export const ShowConfigActionIcon = () => {
  const queryClient = useQueryClient();
  const { data } = useGetConfig();

  return (
    <Tooltip label="Show ByzzBench Configuration">
      <ActionIcon
        onClick={(e) => {
          e.preventDefault();
          openContextModal({
            modal: "showConfig",
            title: "Show Configuration",
            size: "lg",
            innerProps: {},
          });
        }}
      >
        <IconFileSettings />
      </ActionIcon>
    </Tooltip>
  );
};
