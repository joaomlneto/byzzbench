"use client";
import { Grid } from "@mantine/core";
import React from "react";
import { ScheduledFaultsListEntry } from "./ScheduledFaultsListEntry";

export type ScheduledFaultsListProps = {
  scenarioId: number;
  faultIds: string[];
};

export const ScheduledFaultsList = ({
  scenarioId,
  faultIds,
}: ScheduledFaultsListProps) => {
  return (
    <Grid gutter="xs">
      {faultIds.map((faultId) => (
        <Grid.Col span="content" p={0} key={faultId}>
          <ScheduledFaultsListEntry scenarioId={scenarioId} faultId={faultId} />
        </Grid.Col>
      ))}
    </Grid>
  );
};
