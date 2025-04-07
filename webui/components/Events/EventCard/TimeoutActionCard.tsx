"use client";
import { NodeStateNavLink } from "@/components/NodeStateNavLink";
import { TriggerTimeoutAction } from "@/lib/byzzbench-client";
import { Card, CardProps } from "@mantine/core";
import React from "react";

export type TimeoutActionCardProps = {
  action: TriggerTimeoutAction;
} & CardProps;

export const TimeoutActionCard = ({
  action,
  ...otherProps
}: TimeoutActionCardProps) => {
  return (
    <Card p={2} {...otherProps}>
      <NodeStateNavLink
        label={`${action.actionId}: Timeout Node ${action.nodeId}`}
        data={action}
      />
    </Card>
  );
};
