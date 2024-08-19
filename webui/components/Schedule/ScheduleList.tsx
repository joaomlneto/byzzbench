"use client";

import { useSavedSchedulesStore } from "@/hooks/useSavedSchedules";
import { deliverMessage } from "@/lib/byzzbench-client";
import { ActionIcon, Container, Group, Stack, Text } from "@mantine/core";
import { modals } from "@mantine/modals";
import { showNotification } from "@mantine/notifications";
import { IconInfoCircle, IconPlayerPlay, IconX } from "@tabler/icons-react";
import { useQueryClient } from "@tanstack/react-query";
import React, { memo } from "react";

export const ScheduleList = memo(() => {
  const queryClient = useQueryClient();
  const schedules = useSavedSchedulesStore((state) => state.schedules);
  const removeSchedule = useSavedSchedulesStore(
    (state) => state.removeSchedule,
  );

  return (
    <Container size="xs">
      <Stack gap="xs">
        {schedules.map((schedule) => (
          <Group key={schedule.id} justify="space-between">
            <Text>{schedule.id}</Text>
            <Group>
              <ActionIcon
                onClick={() => {
                  modals.openContextModal({
                    title: "Schedule Details",
                    modal: "scheduleDetails",
                    innerProps: { schedule: schedule.schedule },
                  });
                }}
              >
                <IconInfoCircle />
              </ActionIcon>
              <ActionIcon
                onClick={async () => {
                  for (const event of schedule.schedule.events ?? []) {
                    if (event.type === "DeliverEvent") {
                      if (!event.eventId) {
                        showNotification({
                          message: "Event without ID",
                          color: "red",
                        });
                        return;
                      }
                      console.log(`Deliver event #${event.eventId}`);
                      await deliverMessage(event.eventId);
                    }
                  }
                  await queryClient.invalidateQueries();
                }}
              >
                <IconPlayerPlay />
              </ActionIcon>
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
