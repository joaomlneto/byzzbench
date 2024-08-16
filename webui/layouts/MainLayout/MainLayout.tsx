"use client";

import { ColorSchemeToggle } from "@/components/ColorSchemeToggle";
import { DoSchedulerActionIcon } from "@/components/DoSchedulerActionIcon";
import { ForceSyncActionIcon } from "@/components/ForceSyncActionIcon";
import { ResetActionIcon } from "@/components/ResetActionIcon";
import { SaveScheduleActionIcon } from "@/components/SaveScheduleActionIcon";
import { ScenarioSelect } from "@/components/ScenarioSelect";
import { StartActionIcon } from "@/components/StartActionIcon";
import { StopActionIcon } from "@/components/StopActionIcon";
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
