import {
  DeliverMessageActionIcon,
  DropMessageActionIcon,
} from "@/components/messages";
import { NodeStateNavLink } from "@/components/NodeStateNavLink";
import { useGetMessage } from "@/lib/byzzbench-client/generated";
import { Container, Group, JsonInput, Text, Title } from "@mantine/core";
import React, { useMemo } from "react";

export const MessageListEntry = ({ messageId }: { messageId: number }) => {
  const { data: message } = useGetMessage(messageId);

  //const payload = useMemo(() => message?.data.payload, [message?.data.payload]);
  const payload = useMemo(
    () => ({
      // dummy
      __className__: "com.byzzbench.core.messages.Message",
      eventId: message?.data.eventId,
      senderId: message?.data.senderId,
      recipientId: message?.data.recipientId,
      status: "TODO",
    }),
    [message?.data],
  );

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
            {payload.eventId} ({message.data.senderId} -{">"}{" "}
            {payload.recipientId})
          </Title>
          <Text lineClamp={1}>{payload?.__className__?.split(".").pop()}</Text>
        </Group>
        {payload.status == "QUEUED" && (
          <Group gap="xs" wrap="nowrap">
            <DeliverMessageActionIcon messageId={messageId} />
            <DropMessageActionIcon messageId={messageId} />
          </Group>
        )}
      </Group>
      {/*false && <JsonTable data={payloadWithoutClassname} />*/}
      {Object.entries(payloadWithoutClassname).map(([key, value]) => (
        <NodeStateNavLink key={key} label={key} data={value} />
      ))}
    </Container>
  );
};
