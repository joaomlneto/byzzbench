"use client";
import { NodeStateNavLink } from "@/components/NodeStateNavLink";
import { TimeoutEvent } from "@/lib/byzzbench-client";
import { Card, CardProps } from "@mantine/core";
import React from "react";

export type TimeoutEventCardProps = {
  event: TimeoutEvent;
} & CardProps;

export const TimeoutEventCard = ({
  event,
  ...otherProps
}: TimeoutEventCardProps) => {
  return (
    <Card p={2} {...otherProps}>
      <NodeStateNavLink
        label={`${event.eventId}: Timeout Node ${event.recipientId}`}
        data={event}
      />
    </Card>
  );
};
