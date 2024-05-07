"use client";

import { useDropMessage } from "@/lib/bftbench-client/generated";
import { ActionIcon } from "@mantine/core";
import { showNotification } from "@mantine/notifications";
import { IconSendOff } from "@tabler/icons-react";
import { useQueryClient } from "@tanstack/react-query";
import React from "react";

type DropMessageActionIconProps = {
  messageId: number;
};

export const DropMessageActionIcon = ({
  messageId,
}: DropMessageActionIconProps) => {
  const queryClient = useQueryClient();
  const { mutate } = useDropMessage(messageId);

  return (
    <ActionIcon
      onClick={(e) => {
        e.preventDefault();
        mutate(null as never, {
          onSuccess: () => {
            showNotification({
              message: `Message ${messageId} dropped`,
            });
          },
          onError: () => {
            showNotification({
              message: "Failed to drop message",
              color: "red",
            });
          },
          onSettled: async () => {
            await queryClient.invalidateQueries();
          },
        });
      }}
    >
      <IconSendOff />
    </ActionIcon>
  );
};
