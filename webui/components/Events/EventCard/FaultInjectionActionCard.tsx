"use client";
import { NodeStateNavLink } from "@/components/NodeStateNavLink";
import { FaultInjectionAction } from "@/lib/byzzbench-client";
import { Card, CardProps } from "@mantine/core";
import React from "react";

export type FaultInjectionActionCardProps = {
  action: FaultInjectionAction;
} & CardProps;

export const FaultInjectionActionCard = ({
  action,
  ...otherProps
}: FaultInjectionActionCardProps) => {
  return (
    <Card p={2} {...otherProps}>
      <NodeStateNavLink label={`Mutate Message`} data={action} />
    </Card>
  );
};
