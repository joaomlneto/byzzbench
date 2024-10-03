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
import { ImportScheduleButton } from "@/components/ImportScheduleButton";
import { ScheduleList } from "@/components/Schedule";
import { HeaderMenu } from "@/layouts/MainLayout/HeaderMenu";
import { AppShell, Button, Group, Popover, ScrollArea } from "@mantine/core";
import React, { PropsWithChildren } from "react";

export const MainLayout = ({ children }: PropsWithChildren) => {
  return (
    <AppShell
      header={{ height: 60 }}
      aside={{ breakpoint: 0, width: 400 }}
      padding="0"
    >
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
          <ImportScheduleButton size="xs" />
          <Popover>
            <Popover.Target>
              <Button size="xs">Saved Schedules</Button>
            </Popover.Target>
            <Popover.Dropdown>
              <ScrollArea h={500}>
                <ScheduleList />
              </ScrollArea>
            </Popover.Dropdown>
          </Popover>
        </Group>
      </AppShell.Header>

      <AppShell.Main>{children}</AppShell.Main>
    </AppShell>
  );
};
