"use client";

import { JsonTable } from "@/components/JsonTable";
import { NodeStateNavLink } from "@/components/NodeStateNavLink";
import { Container, Grid, Group, Text, Title } from "@mantine/core";
import React from "react";
import { DeliverMessageActionIcon } from "./DeliverMessageActionIcon";
import { DropMessageActionIcon } from "./DropMessageActionIcon";

export type MessagesListProps = {
  messages: {
    messageId: number;
    senderId: string;
    recipientId: string;
    message: Record<string, any>;
  }[];
};

export const MessagesList = ({ messages }: MessagesListProps) => {
  return (
    <Grid gutter="md">
      {Object.entries(messages ?? {})
        .filter(([id, node]) => id !== "__className__")
        .map(([id, message]) => {
          const payload = message.message;
          const payloadClassname = payload.__className__ ?? "";
          const payloadWithoutClassname = Object.fromEntries(
            Object.entries(payload).filter(
              ([key, value]) => key !== "__className__",
            ),
          );
          return (
            <Grid.Col span={3} key={id}>
              <Container fluid style={{ border: "1px solid black" }} p="md">
                <Group justify="space-between" wrap="nowrap" mb="sm">
                  <Group wrap="nowrap">
                    <Title order={4}>
                      {message.messageId} ({message.senderId} -{">"}{" "}
                      {message.recipientId})
                    </Title>
                    <Text lineClamp={1}>
                      {payloadClassname.split(".").pop()}
                    </Text>
                  </Group>
                  <Group gap="xs" wrap="nowrap">
                    <DeliverMessageActionIcon messageId={id} />
                    <DropMessageActionIcon messageId={id} />
                  </Group>
                </Group>
                {false && <JsonTable data={payloadWithoutClassname} />}
                {Object.entries(payloadWithoutClassname).map(([key, value]) => (
                  <NodeStateNavLink label={key} data={value} />
                ))}
              </Container>
            </Grid.Col>
          );
        })}
    </Grid>
  );
};
