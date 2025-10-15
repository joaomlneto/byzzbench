"use client";

import { SchedulerSelect } from "@/components/Scheduler";
import { useExecuteSchedulerAction } from "@/lib/byzzbench-client";
import { ActionIcon, Group, Tooltip } from "@mantine/core";
import { showNotification } from "@mantine/notifications";
import { IconPlayerPlay } from "@tabler/icons-react";
import { useQueryClient } from "@tanstack/react-query";
import React from "react";

export type DoSchedulerActionIconProps = {
  scenarioId: number;
  onChange?: (value: string | null) => void;
};

export const DoSchedulerActionIcon = ({
  scenarioId,
  onChange,
}: DoSchedulerActionIconProps) => {
  const queryClient = useQueryClient();
  const [selected, setSelected] = React.useState<string | null>(null);
  const { mutate } = useExecuteSchedulerAction(selected ?? "", scenarioId);

  return (
    <Group align="end" gap={2}>
      <SchedulerSelect
        label="Exploration Strategy"
        value={selected}
        onChange={(value) => {
          setSelected(value);
          onChange?.(value);
        }}
        size="xs"
      />
      <Tooltip label="Execute next action from strategy">
        <ActionIcon
          onClick={(e) => {
            e.preventDefault();
            mutate(null as never, {
              onSuccess: (e) => {
                showNotification({
                  message:
                    "Executed action: " +
                    JSON.stringify({ scheduler: selected, ...e.data }, null, 2),
                });
              },
              onError: () => {
                showNotification({
                  message: "Failed to schedule next action",
                  color: "red",
                });
              },
              onSettled: async () => {
                await queryClient.invalidateQueries();
              },
            });
          }}
        >
          <IconPlayerPlay />
        </ActionIcon>
      </Tooltip>
    </Group>
  );
};
