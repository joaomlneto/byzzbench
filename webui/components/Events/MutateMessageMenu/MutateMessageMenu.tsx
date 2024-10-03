"use client";

import { MutateMessageMenuEntry } from "@/components/Events/MutateMessageMenu/MutateMessageMenuEntry";
import {
  dropMessage,
  useGetMessageMutators,
} from "@/lib/byzzbench-client/generated";
import { Burger, Loader, Menu, rem } from "@mantine/core";
import { showNotification } from "@mantine/notifications";
import { IconBugFilled, IconTrash } from "@tabler/icons-react";
import { useQueryClient } from "@tanstack/react-query";
import React, { memo } from "react";

type MutateMessageMenuProps = {
  messageId: number;
};

export const MutateMessageMenu = memo(
  ({ messageId }: MutateMessageMenuProps) => {
    const queryClient = useQueryClient();
    const { data, isLoading } = useGetMessageMutators(messageId);

    if (isLoading || !data) {
      return <Loader />;
    }

    return (
      <Menu>
        <Menu.Target>
          <Burger />
        </Menu.Target>
        <Menu.Dropdown>
          <Menu.Label>Mutate and Deliver</Menu.Label>
          {data?.data.map((mutator) => (
            <MutateMessageMenuEntry
              key={mutator}
              messageId={messageId}
              mutatorId={mutator}
            />
          ))}
          <Menu.Item
            color="red"
            leftSection={
              <IconBugFilled style={{ width: rem(14), height: rem(14) }} />
            }
            onClick={(e) => {
              e.preventDefault();
              showNotification({
                color: "red",
                message: "Not yet implemented!",
              });
            }}
          >
            Custom Mutation
          </Menu.Item>

          <Menu.Divider />

          <Menu.Label>Danger zone</Menu.Label>
          <Menu.Item
            color="red"
            leftSection={
              <IconTrash style={{ width: rem(14), height: rem(14) }} />
            }
            onClick={(e) => {
              e.preventDefault();
              dropMessage(messageId)
                .then(() => {
                  showNotification({
                    message: `Message ${messageId} dropped`,
                  });
                })
                .catch(() => {
                  showNotification({
                    message: "Failed to drop message",
                    color: "red",
                  });
                })
                .finally(async () => {
                  await queryClient.invalidateQueries();
                });
            }}
          >
            Drop message
          </Menu.Item>
        </Menu.Dropdown>
      </Menu>
    );
  },
);
MutateMessageMenu.displayName = "MutateMessageMenu";
