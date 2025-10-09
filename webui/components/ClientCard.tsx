"use client";

import { NodeStateNavLink } from "@/components/NodeStateNavLink";
import { useGetScenarioClient } from "@/lib/byzzbench-client";
import { Card, Title } from "@mantine/core";
import React from "react";

export type ClientCardProps = {
  scenarioId: number;
  clientId: string;
};

export const ClientCard = ({ scenarioId, clientId }: ClientCardProps) => {
  const { data } = useGetScenarioClient(scenarioId, clientId);

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
