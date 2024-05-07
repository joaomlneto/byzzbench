import {
  DeliverMessageActionIcon,
  DropMessageActionIcon,
} from "@/components/messages";
import { NodeStateNavLink } from "@/components/NodeStateNavLink";
import { useGetMessage } from "@/lib/bftbench-client/generated";
import { Container, Group, JsonInput, Text, Title } from "@mantine/core";
import React, { useMemo } from "react";

export const MessageListEntry = ({ messageId }: { messageId: number }) => {
  const { data: message } = useGetMessage(messageId);

  const payload = useMemo(() => message?.data.payload, [message?.data.payload]);

  const payloadWithoutClassname = useMemo(() => {
    if (!payload) {
      return {};
    }
    const { __className__, ...rest } = payload;
    return rest;
  }, [payload]);

  if (!message) {
    return "Loading...";
  }

  return (
    <Container fluid style={{ border: "1px solid black" }} p="md">
      {false && <JsonInput value={JSON.stringify(message, null, 2)} autosize />}
      <Group justify="space-between" wrap="nowrap" mb="sm">
        <Group wrap="nowrap">
          <Title order={4}>
            {message.data.messageId} ({message.data.senderId} -{">"}{" "}
            {message.data.recipientId})
          </Title>
          <Text lineClamp={1}>
            {message.data.payload?.__className__?.split(".").pop()}
          </Text>
        </Group>
        <Group gap="xs" wrap="nowrap">
          <DeliverMessageActionIcon messageId={messageId} />
          <DropMessageActionIcon messageId={messageId} />
        </Group>
      </Group>
      {/*false && <JsonTable data={payloadWithoutClassname} />*/}
      {Object.entries(payloadWithoutClassname).map(([key, value]) => (
        <NodeStateNavLink key={key} label={key} data={value} />
      ))}
    </Container>
  );
};
