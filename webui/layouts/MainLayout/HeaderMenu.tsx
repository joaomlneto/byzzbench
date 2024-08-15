"use client";

import { Button, Menu, rem } from "@mantine/core";
import { IconBug, IconSearch, IconSettings } from "@tabler/icons-react";
import { useRouter } from "next/navigation";
import React from "react";

export const HeaderMenu = () => {
  const router = useRouter();
  return (
    <Menu shadow="md" width={200}>
      <Menu.Target>
        <Button>ByzzBench</Button>
      </Menu.Target>

      <Menu.Dropdown>
        <Menu.Item
          leftSection={
            <IconSettings style={{ width: rem(14), height: rem(14) }} />
          }
          onClick={() => router.push("/")}
        >
          Home
        </Menu.Item>
        <Menu.Item
          leftSection={
            <IconSearch style={{ width: rem(14), height: rem(14) }} />
          }
          onClick={() => router.push("/schedules")}
        >
          Saved Schedules
        </Menu.Item>
        <Menu.Divider />
        <Menu.Item
          leftSection={<IconBug style={{ width: rem(14), height: rem(14) }} />}
          onClick={() => router.push("/faults")}
        >
          Faults
        </Menu.Item>
      </Menu.Dropdown>
    </Menu>
  );
};
