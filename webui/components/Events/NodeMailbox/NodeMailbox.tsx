"use client";

import { useGetNodeMailbox } from "@/lib/byzzbench-client";
import { Loader, Stack } from "@mantine/core";
import React, { memo } from "react";
import { NodeMailboxEntry } from ".";

export type NodeMailboxProps = {
  nodeId: string;
};

export const NodeMailbox = memo(({ nodeId }: NodeMailboxProps) => {
  const { data, isLoading } = useGetNodeMailbox(nodeId);

  if (isLoading) {
    return <Loader />;
  }

  return (
    <Stack gap="xs">
      {data?.data.map((messageId) => (
        <NodeMailboxEntry key={messageId} messageId={messageId} />
      ))}
    </Stack>
  );
});
NodeMailbox.displayName = "NodeMailbox";
