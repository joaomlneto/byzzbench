"use client";

import { MessageListEntry } from "@/components/messages/MessagesList/MessageListEntry";
import { Grid } from "@mantine/core";
import React from "react";

export type MessagesListProps = {
  messageIds: number[];
};

export const MessagesList = ({ messageIds }: MessagesListProps) => {
  return (
    <Grid gutter="md">
      {messageIds.map((messageId) => (
        <Grid.Col span={3} key={messageId}>
          <MessageListEntry messageId={messageId} />
        </Grid.Col>
      ))}
    </Grid>
  );
};
