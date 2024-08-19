"use client";
import { NodeStateNavLink } from "@/components/NodeStateNavLink";
import { GenericFaultEvent } from "@/lib/byzzbench-client";
import { Card, CardProps } from "@mantine/core";
import React from "react";

export type FaultInjectionEventCardProps = {
  event: GenericFaultEvent;
} & CardProps;

export const GenericFaultEventCard = ({
  event,
  ...otherProps
}: FaultInjectionEventCardProps) => {
  return (
    <Card p={2} {...otherProps}>
      <NodeStateNavLink
        label={`${event.eventId}: Apply Fault: ${event.payload?.name}`}
        data={event}
      />
    </Card>
  );
};
