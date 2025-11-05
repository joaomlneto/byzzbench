import { useExecuteSpecificSchedulerAction } from "@/lib/byzzbench-client";
import { ActionIcon } from "@mantine/core";
import { showNotification } from "@mantine/notifications";
import { IconSend } from "@tabler/icons-react";
import { useQueryClient } from "@tanstack/react-query";

export type PerformActionActionIconProps = {
  scenarioId: number;
  explorationStrategyId: string;
  actionId: number;
};

export const PerformActionActionIcon = ({
  scenarioId,
  explorationStrategyId,
  actionId,
}: PerformActionActionIconProps) => {
  const queryClient = useQueryClient();
  const { mutate } = useExecuteSpecificSchedulerAction(
    explorationStrategyId,
    scenarioId,
    actionId,
  );

  return (
    <ActionIcon
      onClick={(e) => {
        e.preventDefault();
        mutate(null as never, {
          onSuccess: () => {
            showNotification({
              message: `Action performed`,
            });
          },
          onError: () => {
            showNotification({
              message: "Failed to deliver message",
              color: "red",
            });
          },
          onSettled: async () => {
            await queryClient.invalidateQueries();
          },
        });
      }}
    >
      <IconSend />
    </ActionIcon>
  );
};
