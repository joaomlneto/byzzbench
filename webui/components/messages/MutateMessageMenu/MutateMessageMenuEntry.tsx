"use client";

import {
  useGetMutator,
  useMutateMessage,
} from "@/lib/byzzbench-client/generated";
import { Menu, rem } from "@mantine/core";
import { showNotification } from "@mantine/notifications";
import { IconBug } from "@tabler/icons-react";
import { useQueryClient } from "@tanstack/react-query";
import React from "react";

type MutateMessageMenuEntryProps = {
  messageId: number;
  mutatorId: string;
};

export const MutateMessageMenuEntry = ({
  messageId,
  mutatorId,
}: MutateMessageMenuEntryProps) => {
  const queryClient = useQueryClient();
  const { data } = useGetMutator(mutatorId);

  const { mutate: mutateMessage } = useMutateMessage(messageId, mutatorId);

  return (
    <Menu.Item
      leftSection={<IconBug style={{ width: rem(14), height: rem(14) }} />}
      key={data?.data.name}
      onClick={() => {
        mutateMessage(null as never, {
          onSuccess: () => {
            showNotification({
              message: `Message ${messageId} mutated`,
            });
          },
          onError: () => {
            showNotification({
              message: "Failed to mutate message",
              color: "red",
            });
          },
          onSettled: async () => {
            await queryClient.invalidateQueries();
          },
        });
      }}
    >
      {data?.data.name}
    </Menu.Item>
  );
};
