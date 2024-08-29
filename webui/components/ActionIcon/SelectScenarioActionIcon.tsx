"use client";

import { ActionIcon, Tooltip } from "@mantine/core";
import { openContextModal } from "@mantine/modals";
import { IconPencilPlus } from "@tabler/icons-react";
import React from "react";

export const SelectScenarioActionIcon = () => {
  return (
    <Tooltip label="Select Scenario">
      <ActionIcon
        onClick={(e) => {
          e.preventDefault();
          openContextModal({
            modal: "changeScenario",
            title: "Select Scenario",
            size: "lg",
            innerProps: {},
          });
        }}
      >
        <IconPencilPlus />
      </ActionIcon>
    </Tooltip>
  );
};
