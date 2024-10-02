"use client";

import {
  DoSchedulerActionIcon,
  ForceSyncActionIcon,
  ResetActionIcon,
  SaveScheduleActionIcon,
  SelectScenarioActionIcon,
  StartActionIcon,
  StopActionIcon,
} from "@/components/ActionIcon";
import { ColorSchemeToggle } from "@/components/ColorSchemeToggle";
import { HeaderMenu } from "@/layouts/MainLayout/HeaderMenu";
import { AppShell, Group } from "@mantine/core";
import React, { PropsWithChildren } from "react";

export const MainLayout = ({ children }: PropsWithChildren) => {
  return (
    <AppShell header={{ height: 60 }} padding="0">
      <AppShell.Header>
        <Group p="xs">
          <ColorSchemeToggle />
          <HeaderMenu />
          <SelectScenarioActionIcon />
          <ResetActionIcon />
          <DoSchedulerActionIcon />
          <ForceSyncActionIcon />
          <SaveScheduleActionIcon />
          <StartActionIcon />
          <StopActionIcon />
        </Group>
      </AppShell.Header>

      <AppShell.Main>{children}</AppShell.Main>
    </AppShell>
  );
};
