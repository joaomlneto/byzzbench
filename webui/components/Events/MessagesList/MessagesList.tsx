"use client";

import { MessageListEntry } from "@/components/Events/MessagesList/MessageListEntry";
import { Grid } from "@mantine/core";

export type MessagesListProps = {
  messageIds: number[];
};

export const MessagesList = ({ messageIds }: MessagesListProps) => {
  return (
    <Grid gutter="md">
      {messageIds.map((messageId) => (
        <Grid.Col span="content" key={messageId}>
          <MessageListEntry messageId={messageId} />
        </Grid.Col>
      ))}
    </Grid>
  );
};
