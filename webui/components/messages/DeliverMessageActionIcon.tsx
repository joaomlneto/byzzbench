"use client";

import { usePostMessageMessageidDeliver } from "@/lib/bftbench-client";
import { ActionIcon } from "@mantine/core";
import { showNotification } from "@mantine/notifications";
import { IconSend } from "@tabler/icons-react";
import { useQueryClient } from "@tanstack/react-query";

type DeliverMessageActionIconProps = {
  messageId: string;
};

export const DeliverMessageActionIcon = ({
  messageId,
}: DeliverMessageActionIconProps) => {
  const queryClient = useQueryClient();
  const { mutate } = usePostMessageMessageidDeliver(messageId);

  return (
    <ActionIcon
      onClick={(e) => {
        e.preventDefault();
        mutate(null as never, {
          onSuccess: () => {
            showNotification({
              message: `Message ${messageId} delivered`,
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
