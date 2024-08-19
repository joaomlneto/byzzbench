"use client";

import { NodeStateNavLink } from "@/components/NodeStateNavLink";
import { useGetClient } from "@/lib/byzzbench-client";
import { Card, Title } from "@mantine/core";
import React from "react";

export type ClientCardProps = {
  clientId: string;
};

export const ClientCard = ({ clientId }: ClientCardProps) => {
  const { data } = useGetClient(clientId);

  return (
    <Card p="md" style={{ maxWidth: "400px" }}>
      {data && (
        <NodeStateNavLink
          data={data.data}
          label={<Title order={4}>{clientId}</Title>}
          defaultOpened
          opened={true}
        />
      )}
    </Card>
  );
};
