"use client";

import { DeliverMessageActionIcon } from "@/components/ActionIcon";
import { MutateMessageMenu } from "@/components/Events/MutateMessageMenu";
import { NodeStateNavLink } from "@/components/NodeStateNavLink";
import { useGetMessage } from "@/lib/byzzbench-client/generated";
import { Badge, Card, Collapse, Group, Loader, Text } from "@mantine/core";
import { useDisclosure } from "@mantine/hooks";
import React, { memo } from "react";

export type NodeMailboxEntryProps = {
  scenarioId: number;
  messageId: number;
};

export const NodeMailboxEntry = memo(
  ({ scenarioId, messageId }: NodeMailboxEntryProps) => {
    const [opened, { toggle }] = useDisclosure(false);
    const { data: message, isLoading } = useGetMessage(scenarioId, messageId);

    if (isLoading || !message) {
      return <Loader />;
    }

    return (
      <Card withBorder shadow="xs" px="xs" py={2} m={0}>
        <Group justify="space-between" wrap="nowrap">
          <Group
            wrap="nowrap"
            gap="xs"
            grow
            onClick={(e) => {
              e.preventDefault();
              toggle();
            }}
          >
            <Text span c="dimmed">
              {message.data.eventId}
            </Text>
            <Badge size="xs">
              {
                // @ts-expect-error: senderId is not in all subtypes of Event
                message.data.senderId
              }
            </Badge>
            <Group grow>
              <Text lineClamp={1} w="200px">
                {
                  // @ts-ignore
                  message.data.payload?.type ?? message.data.type ?? "???"
                }
              </Text>
            </Group>
          </Group>
          {message.data.status == "QUEUED" && (
            <Group gap="xs" wrap="nowrap">
              <DeliverMessageActionIcon
                scenarioId={scenarioId}
                messageId={messageId}
              />
              <MutateMessageMenu
                scenarioId={scenarioId}
                messageId={messageId}
              />
            </Group>
          )}
        </Group>
        <Collapse in={opened}>
          {Object.entries(message?.data ?? {}).map(([key, value]) => (
            <NodeStateNavLink
              key={key}
              label={key}
              data={opened ? value : {}}
            />
          ))}
        </Collapse>
      </Card>
    );
  },
);

NodeMailboxEntry.displayName = "NodeMailboxEntry";
