"use client";

import {
  DoSchedulerActionIcon,
  ForceSyncActionIcon,
  ResetActionIcon,
  SaveScheduleActionIcon,
  StartActionIcon,
  StopActionIcon,
} from "@/components/ActionIcon";
import { ColorSchemeToggle } from "@/components/ColorSchemeToggle";
import { ScenarioSelect } from "@/components/ScenarioSelect";
import { HeaderMenu } from "@/layouts/MainLayout/HeaderMenu";
import { AppShell, Group } from "@mantine/core";
import { useDisclosure } from "@mantine/hooks";
import React, { PropsWithChildren } from "react";

export const MainLayout = ({ children }: PropsWithChildren) => {
  const [opened, { toggle }] = useDisclosure();

  return (
    <AppShell header={{ height: 60 }} padding="0">
      <AppShell.Header>
        <Group p="xs">
          <ColorSchemeToggle />
          <HeaderMenu />
          <ScenarioSelect />
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
