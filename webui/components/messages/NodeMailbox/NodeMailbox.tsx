"use client";

import { NodeMailboxEntry } from "@/components/messages";
import { useGetNodeMailbox } from "@/lib/byzzbench-client";
import { Spoiler, Stack } from "@mantine/core";
import React, { useMemo } from "react";

export type NodeMailboxProps = {
  nodeId: string;
};

export const NodeMailbox = ({ nodeId }: NodeMailboxProps) => {
  const { data } = useGetNodeMailbox(nodeId);

  const messageIds = useMemo(() => data?.data ?? [], [data?.data]);

  return (
    <Spoiler maxHeight={300} showLabel="Show more" hideLabel="Show less">
      <Stack gap="xs">
        {messageIds.map((messageId) => (
          <NodeMailboxEntry key={messageId} messageId={messageId} />
        ))}
      </Stack>
    </Spoiler>
  );
};
