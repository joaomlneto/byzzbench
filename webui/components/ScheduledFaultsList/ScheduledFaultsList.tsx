"use client";
import { Grid } from "@mantine/core";
import React from "react";
import { ScheduledFaultsListEntry } from "./ScheduledFaultsListEntry";

export type FaultsListProps = {
  faultIds: string[];
};

export const ScheduledFaultsList = ({ faultIds }: FaultsListProps) => {
  return (
    <Grid gutter="xs">
      {faultIds.map((faultId) => (
        <Grid.Col span="content" p={0} key={faultId}>
          <ScheduledFaultsListEntry faultId={faultId} />
        </Grid.Col>
      ))}
    </Grid>
  );
};
