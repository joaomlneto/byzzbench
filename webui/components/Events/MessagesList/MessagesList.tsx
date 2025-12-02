"use client";

import { MessageListEntry } from "@/components/Events/MessagesList/MessageListEntry";
import { Grid } from "@mantine/core";

export type MessagesListProps = {
  scenarioId: number;
  messageIds: number[];
};

export const MessagesList = ({ scenarioId, messageIds }: MessagesListProps) => {
  return (
    <Grid gutter="md">
      {messageIds.map((messageId) => (
        <Grid.Col span="content" key={messageId}>
          <MessageListEntry scenarioId={scenarioId} messageId={messageId} />
        </Grid.Col>
      ))}
    </Grid>
  );
};
