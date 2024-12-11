"use client";

import { NodeMailbox } from "@/components/Events/NodeMailbox";
import { NodeStateNavLink } from "@/components/NodeStateNavLink";
import { useGetNode, useGetPartitions } from "@/lib/byzzbench-client";
import {
  Card,
  Group,
  Loader,
  Space,
  Text,
  Title,
  Tooltip,
} from "@mantine/core";
import React from "react";

export type NodeCardProps = {
  nodeId: string;
};

export const NodeCard = ({ nodeId }: NodeCardProps) => {
  const { data, isLoading } = useGetNode(nodeId);
  const partitionsQuery = useGetPartitions();

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
      {/*{data && (*/}
      {/*  <NodeStateNavLink*/}
      {/*    data={data.data}*/}
      {/*    label={*/}
      {/*      <Group justify="space-between">*/}
      {/*        <Tooltip label="Node ID">*/}
      {/*          <Title order={4}>{nodeId}</Title>*/}
      {/*        </Tooltip>*/}
      {/*        {partitionsQuery.data?.data[nodeId] && (*/}
      {/*          <Tooltip label="Network partition ID">*/}
      {/*            <Text>P{partitionsQuery.data.data[nodeId] ?? 0}</Text>*/}
      {/*          </Tooltip>*/}
      {/*        )}*/}
      {/*      </Group>*/}
      {/*    }*/}
      {/*    defaultOpened*/}
      {/*    opened={false}*/}
      {/*  />*/}
      {/*)}*/}
      <Space h="xs" />
      <NodeMailbox nodeId={nodeId} />
    </Card>
  );
};
