"use client";

import {
  DeliverMessageActionIcon,
  DropMessageActionIcon,
} from "@/components/ActionIcon";
import { NodeStateNavLink } from "@/components/NodeStateNavLink";
import { useGetMessage } from "@/lib/byzzbench-client/generated";
import { Collapse, Container, Group, Text, Title } from "@mantine/core";
import { useDisclosure } from "@mantine/hooks";
import React, { useMemo } from "react";

export const MessageListEntry = ({ messageId }: { messageId: number }) => {
  const [opened, { toggle }] = useDisclosure(false);
  const { data: message } = useGetMessage(messageId);

  //const payload = useMemo(() => message?.data.payload, [message?.data.payload]);
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
    <Container fluid w="100%" style={{ border: "1px solid black" }} p="xs">
      <Group
        justify="space-between"
        wrap="nowrap"
        mb="sm"
        onClick={(e) => {
          e.preventDefault();
          toggle();
        }}
      >
        <Group wrap="nowrap">
          <Title order={4}>
            {payload?.eventId} (
            {
              // @ts-expect-error: senderId is not in all subtypes of Event
              message.data.senderId
            }{" "}
            -{">"}{" "}
            {
              // @ts-expect-error: recipientId is not in all subtypes of Event
              payload?.recipientId
            }
            )
          </Title>
          <Text lineClamp={1}>{payload?.type?.split(".").pop()}</Text>
        </Group>
        {payload?.status == "QUEUED" && (
          <Group gap="xs" wrap="nowrap">
            <DeliverMessageActionIcon messageId={messageId} />
            <DropMessageActionIcon messageId={messageId} />
          </Group>
        )}
      </Group>
      <Collapse in={opened}>
        {/*false && <JsonTable data={payloadWithoutClassname} />*/}
        {Object.entries(payloadWithoutClassname ?? {}).map(([key, value]) => (
          <NodeStateNavLink key={key} label={key} data={value} />
        ))}
      </Collapse>
    </Container>
  );
};
