"use client";

import { ColorSchemeToggle } from "@/components/ColorSchemeToggle";
import { DoSchedulerActionIcon } from "@/components/DoSchedulerActionIcon";
import { ResetActionIcon } from "@/components/ResetActionIcon";
import { AppShell, Group, Text } from "@mantine/core";
import { useDisclosure } from "@mantine/hooks";
import React, { PropsWithChildren } from "react";

export const MainLayout = ({ children }: PropsWithChildren) => {
  const [opened, { toggle }] = useDisclosure();

  return (
    <AppShell header={{ height: 60 }} padding="0">
      <AppShell.Header>
        <Group p="xs">
          <ColorSchemeToggle />
          <Text fw={900}>ByzzBench</Text>
          <ResetActionIcon />
          <DoSchedulerActionIcon />
        </Group>
      </AppShell.Header>

      <AppShell.Main>{children}</AppShell.Main>
    </AppShell>
  );
};
