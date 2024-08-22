"use client";

import { DeliverMessageActionIcon } from "@/components/ActionIcon";
import { MutateMessageMenu } from "@/components/Events/MutateMessageMenu";
import { NodeStateNavLink } from "@/components/NodeStateNavLink";
import { useGetMessage } from "@/lib/byzzbench-client/generated";
import { Badge, Card, Collapse, Group, Text } from "@mantine/core";
import { useDisclosure } from "@mantine/hooks";
import React, { memo } from "react";

export const NodeMailboxEntry = memo(({ messageId }: { messageId: number }) => {
  const [opened, { toggle }] = useDisclosure(false);
  const { data: message } = useGetMessage(messageId);

  if (!message?.data) {
    return null;
  }

  return (
    <Card withBorder shadow="xs" px="xs" py={2} m={0}>
      <Group justify="space-between" wrap="nowrap">
        <Group
          wrap="nowrap"
          gap="xs"
          onClick={(e) => {
            e.preventDefault();
            toggle();
          }}
        >
          <Text span c="dimmed">
            {message.data.eventId}
          </Text>
          <Badge size="sm">{message.data.senderId}</Badge>
          <Text lineClamp={1}>
            {
              // @ts-ignore
              message.data.payload?.type ?? message.data.type ?? "???"
            }
          </Text>
        </Group>
        {message.data.status == "QUEUED" && (
          <Group gap="xs" wrap="nowrap">
            {true && <DeliverMessageActionIcon messageId={messageId} />}
            {true && <MutateMessageMenu messageId={messageId} />}
          </Group>
        )}
      </Group>
      <Collapse in={opened}>
        {Object.entries(message.data ?? {}).map(([key, value]) => (
          <NodeStateNavLink key={key} label={key} data={value} />
        ))}
      </Collapse>
    </Card>
  );
});

NodeMailboxEntry.displayName = "NodeMailboxEntry";
