"use client";
import { NodeStateNavLink } from "@/components/NodeStateNavLink";
import { DeliverMessageAction } from "@/lib/byzzbench-client";
import { Card, CardProps } from "@mantine/core";
import React from "react";

export type DeliverMessageActionCardProps = {
  action: DeliverMessageAction;
} & CardProps;

export const DeliverMessageActionCard = ({
  action,
  ...otherProps
}: DeliverMessageActionCardProps) => {
  return (
    <Card p={2} {...otherProps}>
      <NodeStateNavLink
        label={`${action.actionId}: Deliver ${action.payload?.type} from ${action.senderId} to ${action.recipientId}`}
        data={action}
      />
    </Card>
  );
};
