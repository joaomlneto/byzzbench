"use client";

import { useGetScenarioNodeMailbox } from "@/lib/byzzbench-client";
import { Loader, Stack } from "@mantine/core";
import React, { memo } from "react";
import { NodeMailboxEntry } from ".";

export type NodeMailboxProps = {
  scenarioId: number;
  nodeId: string;
};

export const NodeMailbox = memo(({ scenarioId, nodeId }: NodeMailboxProps) => {
  const { data, isLoading } = useGetScenarioNodeMailbox(scenarioId, nodeId);

  if (isLoading) {
    return <Loader />;
  }

  return (
    <Stack gap="xs">
      {data?.data.map((messageId) => (
        <NodeMailboxEntry
          scenarioId={scenarioId}
          key={messageId}
          messageId={messageId}
        />
      ))}
    </Stack>
  );
});
NodeMailbox.displayName = "NodeMailbox";
