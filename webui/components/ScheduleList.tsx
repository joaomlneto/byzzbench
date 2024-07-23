"use client";

import { useSavedSchedulesStore } from "@/hooks/useSavedSchedules";
import { ActionIcon, Container, Group, Stack, Text } from "@mantine/core";
import { modals } from "@mantine/modals";
import { IconPlayerPlay, IconX } from "@tabler/icons-react";
import React from "react";

export const ScheduleList = () => {
  const schedules = useSavedSchedulesStore((state) => state.schedules);
  const removeSchedule = useSavedSchedulesStore(
    (state) => state.removeSchedule,
  );
  return (
    <Container size="xs">
      <Stack gap="xs">
        {schedules.map((schedule) => (
          <Group key={schedule.name} justify="space-between">
            <Text>{schedule.name}</Text>
            <Group>
              <ActionIcon>
                <IconPlayerPlay />
              </ActionIcon>
              <ActionIcon
                onClick={() =>
                  modals.openConfirmModal({
                    title: "Delete Schedule?",
                    children: `Are you sure you want to delete the ${schedule.name} schedule?`,
                    labels: { cancel: "Cancel", confirm: "Delete" },
                    onConfirm: () => removeSchedule(schedule),
                  })
                }
              >
                <IconX />
              </ActionIcon>
            </Group>
          </Group>
        ))}
      </Stack>
    </Container>
  );
};
