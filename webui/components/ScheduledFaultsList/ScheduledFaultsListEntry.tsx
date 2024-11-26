"use client";
import { useGetAutomaticFault } from "@/lib/byzzbench-client";
import { Card, Group, Text } from "@mantine/core";
import React from "react";

export type FaultsListEntryProps = {
  faultId: string;
  disabled?: boolean;
};

export const ScheduledFaultsListEntry = ({ faultId }: FaultsListEntryProps) => {
  const faultQuery = useGetAutomaticFault(faultId);
  return (
    <Card withBorder shadow="sm" padding="xs" m={4} maw="350">
      <Group gap="xs">
        <Text>{faultQuery.data?.data.name}</Text>
      </Group>
    </Card>
  );
};
