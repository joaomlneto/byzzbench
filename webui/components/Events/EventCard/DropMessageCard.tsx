"use client";
import { NodeStateNavLink } from "@/components/NodeStateNavLink";
import { DropMessageAction } from "@/lib/byzzbench-client";
import { CardProps } from "@mantine/core";
import React from "react";

export type DropMessageCardProps = {
  action: DropMessageAction;
} & CardProps;

export const DropMessageCard = ({
  action,
  ...otherProps
}: DropMessageCardProps) => {
  return (
    <NodeStateNavLink label={`Drop message ${action.eventId}`} data={action} />
  );
};
