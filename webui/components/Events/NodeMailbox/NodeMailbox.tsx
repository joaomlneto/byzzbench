"use client";

import { useGetNodeMailbox } from "@/lib/byzzbench-client";
import { Stack } from "@mantine/core";
import React, { memo, useMemo } from "react";
import { NodeMailboxEntry } from ".";

export type NodeMailboxProps = {
  nodeId: string;
};

export const NodeMailbox = memo(({ nodeId }: NodeMailboxProps) => {
  const { data } = useGetNodeMailbox(nodeId);

  const messageIds = useMemo(() => data?.data ?? [], [data?.data]);

  return (
    <Stack gap="xs">
      {messageIds.map((messageId) => (
        <NodeMailboxEntry key={messageId} messageId={messageId} />
      ))}
    </Stack>
  );
});
NodeMailbox.displayName = "NodeMailbox";
