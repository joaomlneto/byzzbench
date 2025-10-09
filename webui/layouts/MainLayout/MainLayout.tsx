"use client";

import { ForceSyncActionIcon } from "@/components/ActionIcon";
import { ShowConfigActionIcon } from "@/components/ActionIcon/ShowConfigActionIcon";
import { ColorSchemeToggle } from "@/components/ColorSchemeToggle";
import { ImportScheduleButton } from "@/components/ImportScheduleButton";
import { ScheduleList } from "@/components/Schedule";
import {
  ActionIcon,
  Anchor,
  AppShell,
  Button,
  Group,
  Popover,
  ScrollArea,
} from "@mantine/core";
import { IconHome } from "@tabler/icons-react";
import Link from "next/link";
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
          <Anchor component={Link} href="/">
            <ActionIcon>
              <IconHome size="m" />
            </ActionIcon>
          </Anchor>
          <ShowConfigActionIcon />
          <ForceSyncActionIcon />
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
