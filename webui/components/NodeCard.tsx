"use client";

import { NodeMailbox } from "@/components/Events/NodeMailbox";
import { NodeStateNavLink } from "@/components/NodeStateNavLink";
import {
  useGetFaultyReplicas,
  useGetNode,
  useGetPartitions,
} from "@/lib/byzzbench-client";
import {
  Card,
  Group,
  Loader,
  Space,
  Text,
  Title,
  Tooltip,
} from "@mantine/core";
import { IconBug } from "@tabler/icons-react";
import React from "react";

export type NodeCardProps = {
  nodeId: string;
  showMailboxes?: boolean;
};

export const NodeCard = ({ nodeId, showMailboxes = true }: NodeCardProps) => {
  const { data, isLoading } = useGetNode(nodeId);
  const faultyReplicasQuery = useGetFaultyReplicas();
  const partitionsQuery = useGetPartitions();

  const isFaulty = faultyReplicasQuery.data?.data.includes(nodeId);

  if (isLoading || partitionsQuery.isLoading) {
    return <Loader />;
  }

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
              <Group>
                <Tooltip label="Node ID">
                  <Title order={4}>{nodeId}</Title>
                </Tooltip>
                {isFaulty && (
                  <Tooltip label="Replica is marked faulty">
                    <IconBug size={18} color="red" />
                  </Tooltip>
                )}
              </Group>
              {partitionsQuery.data?.data[nodeId] && (
                <Tooltip label="Network partition ID">
                  <Text>P{partitionsQuery.data.data[nodeId] ?? 0}</Text>
                </Tooltip>
              )}
            </Group>
          }
          defaultOpened
          opened={true}
        />
      )}
      <Space h="xs" />
      {showMailboxes && <NodeMailbox nodeId={nodeId} />}
    </Card>
  );
};
