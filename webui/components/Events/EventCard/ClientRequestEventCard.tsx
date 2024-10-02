"use client";
import { NodeStateNavLink } from "@/components/NodeStateNavLink";
import { ClientRequestEvent } from "@/lib/byzzbench-client";
import { Card, CardProps } from "@mantine/core";
import React from "react";

export type ClientRequestEventCardProps = {
  event: ClientRequestEvent;
} & CardProps;

export const ClientRequestEventCard = ({
  event,
  ...otherProps
}: ClientRequestEventCardProps) => {
  return (
    <Card p={2} {...otherProps}>
      <NodeStateNavLink
        label={`${event.eventId}: Client Request from ${event.senderId} to ${event.recipientId}`}
        data={event}
      />
    </Card>
  );
};
