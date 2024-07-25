"use client";

import { useSavedSchedulesStore } from "@/hooks/useSavedSchedules";
import { deliverMessage } from "@/lib/byzzbench-client";
import { ActionIcon, Container, Group, Stack, Text } from "@mantine/core";
import { modals } from "@mantine/modals";
import { IconInfoCircle, IconPlayerPlay, IconX } from "@tabler/icons-react";
import { useQueryClient } from "@tanstack/react-query";
import React from "react";

export const ScheduleList = () => {
  const queryClient = useQueryClient();
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
              <ActionIcon
                onClick={() => {
                  modals.openContextModal({
                    title: "Schedule Details",
                    modal: "scheduleDetails",
                    innerProps: { schedule },
                  });
                }}
              >
                <IconInfoCircle />
              </ActionIcon>
              <ActionIcon
                onClick={async () => {
                  for (const action of schedule.actions) {
                    if (action.type === "DeliverEvent") {
                      console.log(`Deliver event #${action.event.eventId}`);
                      await deliverMessage(action.event.eventId);
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
