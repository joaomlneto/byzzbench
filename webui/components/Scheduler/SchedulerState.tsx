"use client";

import { NodeStateNavLink } from "@/components/NodeStateNavLink";
import { useGetScheduler } from "@/lib/byzzbench-client";
import { JsonInputProps } from "@mantine/core";
import React from "react";

export type SchedulerStateProps = JsonInputProps & {
  schedulerId: string;
};

export const SchedulerState = ({
  schedulerId,
  ...otherProps
}: SchedulerStateProps) => {
  const { data } = useGetScheduler(schedulerId);
  // hide config from state view - its available elsewhere
  const state = { ...data?.data, config: undefined };

  return (
    <NodeStateNavLink
      label={`Exploration Strategy Metadata`}
      data={state}
      opened
    />
  );
};
