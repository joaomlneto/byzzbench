"use client";

import { NodeMailbox } from "@/components/Events/NodeMailbox";
import { NodeStateNavLink } from "@/components/NodeStateNavLink";
import { useGetNode, useGetPartitions } from "@/lib/byzzbench-client";
import { Card, Group, Space, Text, Title, Tooltip } from "@mantine/core";
import React, { useMemo } from "react";

export type NodeCardProps = {
  nodeId: string;
};

export const NodeCard = ({ nodeId }: NodeCardProps) => {
  const { data } = useGetNode(nodeId);
  const partitionsQuery = useGetPartitions();

  const partitionId = useMemo(() => {
    return partitionsQuery.data?.data[nodeId] ?? 0;
  }, [nodeId, partitionsQuery.data?.data]);

  return (
    <Card
      withBorder
      shadow="sm"
      padding="xs"
      m="xs"
      style={{ minWidth: 350, maxWidth: 400 }}
    >
      {data && (
        <NodeStateNavLink
          data={data.data}
          label={
            <Group justify="space-between">
              <Tooltip label="Node ID">
                <Title order={4}>{nodeId}</Title>
              </Tooltip>
              <Tooltip label="Network partition ID">
                <Text>P{partitionId}</Text>
              </Tooltip>
            </Group>
          }
          defaultOpened
          opened={true}
        />
      )}
      <Space h="xs" />
      <NodeMailbox nodeId={nodeId} />
    </Card>
  );
};
