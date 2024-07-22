import { DeliverMessageActionIcon } from "@/components/messages";
import { MutateMessageMenu } from "@/components/messages/MutateMessageMenu";
import { NodeStateNavLink } from "@/components/NodeStateNavLink";
import { useGetMessage } from "@/lib/byzzbench-client/generated";
import { Badge, Collapse, Container, Group, Text } from "@mantine/core";
import { useDisclosure } from "@mantine/hooks";
import React, { useMemo } from "react";

export const NodeMailboxEntry = ({ messageId }: { messageId: number }) => {
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
        <Group wrap="nowrap" gap="xs">
          <Text span c="dimmed">
            {payload?.eventId}
          </Text>
          <Badge size="sm">{message.data.senderId}</Badge>
          <Text lineClamp={1}>
            {payload?.payload?.type ?? payload?.type ?? "???"}
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
    </Container>
  );
};
