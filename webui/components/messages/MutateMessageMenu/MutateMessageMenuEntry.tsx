"use client";

import { useGetMutator } from "@/lib/byzzbench-client/generated";
import { Menu, rem } from "@mantine/core";
import { IconBug } from "@tabler/icons-react";
import { useQueryClient } from "@tanstack/react-query";
import React from "react";

type MutateMessageMenuEntryProps = {
  mutatorId: number;
};

export const MutateMessageMenuEntry = ({
  mutatorId,
}: MutateMessageMenuEntryProps) => {
  const queryClient = useQueryClient();
  const { data } = useGetMutator(mutatorId);

  return (
    <Menu.Item
      leftSection={<IconBug style={{ width: rem(14), height: rem(14) }} />}
      key={data?.data.name}
      /*
      onClick={() => {
        mutator.mutate(null as never, {
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
      }}*/
    >
      {data?.data.name}
    </Menu.Item>
  );
};
