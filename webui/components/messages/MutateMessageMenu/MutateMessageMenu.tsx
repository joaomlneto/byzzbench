"use client";

import { MutateMessageMenuEntry } from "@/components/messages/MutateMessageMenu/MutateMessageMenuEntry";
import {
  useDropMessage,
  useGetMessageMutators,
} from "@/lib/byzzbench-client/generated";
import { ActionIcon, Burger, Menu, rem } from "@mantine/core";
import { showNotification } from "@mantine/notifications";
import { IconBugFilled, IconSend, IconTrash } from "@tabler/icons-react";
import { useQueryClient } from "@tanstack/react-query";
import React from "react";

type MutateMessageMenuProps = {
  messageId: number;
};

export const MutateMessageMenu = ({ messageId }: MutateMessageMenuProps) => {
  const queryClient = useQueryClient();
  const { data } = useGetMessageMutators(messageId);
  const { mutate: dropMessage } = useDropMessage(messageId);

  return (
    <Menu>
      <Menu.Target>
        <Burger />
      </Menu.Target>
      <Menu.Dropdown>
        <Menu.Label>Mutate and Deliver</Menu.Label>
        {data?.data.map((mutator) => (
          <MutateMessageMenuEntry key={mutator} mutatorId={mutator} />
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
            dropMessage(null as never, {
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
          Drop message
        </Menu.Item>
      </Menu.Dropdown>
    </Menu>
  );

  return (
    <ActionIcon
      onClick={(e) => {
        e.preventDefault();
        const mutate = (a: any, b: any) => {};
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
