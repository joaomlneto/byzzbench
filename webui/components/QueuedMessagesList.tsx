"use client";

import {DeliverMessageActionIcon} from "@/components/DeliverMessageActionIcon";
import {JsonTable} from "@/components/JsonTable";
import {useGetCaptivemessages} from "@/lib/bftbench-client";
import {Container, Grid, Group, JsonInput, Text, Title} from "@mantine/core";
import React from "react";

export const QueuedMessagesList = () => {
  const { data } = useGetCaptivemessages({ query: { refetchInterval: 1000 } });
  return (
    <Grid gutter="md">
      {Object.entries(data?.data ?? {})
        .filter(([id, node]) => id !== "__className__")
        .map(([id, message]) => {
            const payload = message.message;
            const payloadClassname = payload.__className__;
            const payloadWithoutClassname = Object.fromEntries(
                Object.entries(payload).filter(([key, value]) => key !== "__className__")
            );
          return (
            <Grid.Col span="auto" key={id}>
              <Container fluid style={{border: "1px solid black"}} p="md">
                <Group justify="space-between" wrap="nowrap" mb="sm">
                  <Group wrap="nowrap">
                    <Title order={4}>{id} ({message.senderId} -> {message.recipientId})</Title>
                    <Text>{payloadClassname.split(".").pop()}</Text>
                  </Group>
                    <DeliverMessageActionIcon messageId={id} />
                </Group>
                <JsonTable data={payloadWithoutClassname}/>
                {false && <JsonInput
                  value={JSON.stringify(payloadWithoutClassname, null, 2)}
                  autosize
                  maxRows={10}
                />}
              </Container>
            </Grid.Col>
          );
        })}
      {false && <JsonInput
          value={JSON.stringify(data?.data, null, 2)}
          autosize
          maxRows={10}
      />}
    </Grid>
  );
};
