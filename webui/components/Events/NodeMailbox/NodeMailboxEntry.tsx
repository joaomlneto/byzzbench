"use client";

import { DeliverMessageActionIcon } from "@/components/ActionIcon";
import { MutateMessageMenu } from "@/components/Events/MutateMessageMenu";
import { NodeStateNavLink } from "@/components/NodeStateNavLink";
import { useGetMessage } from "@/lib/byzzbench-client/generated";
import { Badge, Card, Collapse, Group, Text, Tooltip } from "@mantine/core";
import { useDisclosure } from "@mantine/hooks";
import React, { useMemo } from "react";

export const NodeMailboxEntry = ({ messageId }: { messageId: number }) => {
  const [opened, { toggle }] = useDisclosure(false);
  const { data: message } = useGetMessage(messageId);
  const payload = useMemo(() => message?.data, [message?.data]);

  const payloadWithoutClassname = useMemo(() => {
    if (!payload) {
      return null;
    }
    const { type, ...rest } = payload;
    return rest;
  }, [payload]);

  if (!message) {
    return "Loading...";
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
            {payload?.eventId}
          </Text>
          <Tooltip label="Sender">
            <Badge size="sm">{message.data.senderId}</Badge>
          </Tooltip>
          <Text lineClamp={1}>
            {
              // @ts-ignore
              payload?.payload?.type ?? payload?.type ?? "???"
            }
          </Text>
        </Group>
        {payload?.status == "QUEUED" && (
          <Group gap="xs" wrap="nowrap">
            <DeliverMessageActionIcon messageId={messageId} />
            <MutateMessageMenu messageId={messageId} />
          </Group>
        )}
      </Group>
      <Collapse in={opened}>
        {/*false && <JsonTable data={payloadWithoutClassname} />*/}
        {Object.entries(payloadWithoutClassname ?? {}).map(([key, value]) => (
          <NodeStateNavLink key={key} label={key} data={value} />
        ))}
      </Collapse>
    </Card>
  );
};
