"use client";
import { FaultsListEntry } from "@/components/FaultsList/FaultsListEntry";
import { Grid } from "@mantine/core";
import React from "react";

export type FaultsListProps = {
  faultIds: string[];
};

export const FaultsList = ({ faultIds }: FaultsListProps) => {
  return (
    <Grid gutter="md">
      {faultIds.map((faultId) => (
        <Grid.Col span="content" key={faultId}>
          <FaultsListEntry faultId={faultId} />
        </Grid.Col>
      ))}
    </Grid>
  );
};
