"use client";

import { ScheduleDetails } from "@/components/Schedule/ScheduleDetails";
import { useSavedSchedulesStore } from "@/hooks/useSavedSchedules";
import { ActionIcon, Container, Group, Stack } from "@mantine/core";
import { modals } from "@mantine/modals";
import { IconX } from "@tabler/icons-react";
import React, { memo } from "react";

export const ScheduleList = memo(() => {
  const schedules = useSavedSchedulesStore((state) => state.schedules);
  const removeSchedule = useSavedSchedulesStore(
    (state) => state.removeSchedule,
  );

  return (
    <Container size="xs">
      <Stack gap="sm">
        {schedules.map((schedule) => (
          <Group key={schedule.id} justify="space-between">
            <ScheduleDetails
              title={schedule.id}
              schedule={schedule.schedule}
              hideSchedule
              hideSaveButton
            />
            <Group>
              <ActionIcon
                onClick={() =>
                  modals.openConfirmModal({
                    title: "Delete Schedule?",
                    children: `Are you sure you want to delete the ${schedule.id} schedule?`,
                    labels: { cancel: "Cancel", confirm: "Delete" },
                    onConfirm: () => removeSchedule(schedule.id),
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
});
ScheduleList.displayName = "ScheduleList";
