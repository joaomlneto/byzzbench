"use client";

import { NodeMailbox } from "@/components/messages/NodeMailbox";
import { NodeStateNavLink } from "@/components/NodeStateNavLink";
import { useGetNode } from "@/lib/byzzbench-client";
import { Container, JsonInput, Stack, Title } from "@mantine/core";
import React from "react";

export type NodeCardProps = {
  nodeId: string;
};

export const NodeCard = ({ nodeId }: NodeCardProps) => {
  const { data } = useGetNode(nodeId);

  return (
    <Container
      fluid
      style={{ border: "1px solid black", minWidth: 400, maxWidth: 600 }}
      p="md"
    >
      <Stack gap="xs">
        {false && (
          <JsonInput value={JSON.stringify(data?.data, null, 2)} autosize />
        )}
        {data && (
          <NodeStateNavLink
            data={data.data}
            label={<Title order={4}>{nodeId}</Title>}
            defaultOpened
            opened={true}
          />
        )}
        <NodeMailbox nodeId={nodeId} />
      </Stack>
    </Container>
  );
};
