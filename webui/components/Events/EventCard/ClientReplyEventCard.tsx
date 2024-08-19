import { NodeStateNavLink } from "@/components/NodeStateNavLink";
import { ClientReplyEvent } from "@/lib/byzzbench-client";
import { Card, CardProps } from "@mantine/core";
import React from "react";

export type ClientReplyEventCardProps = {
  event: ClientReplyEvent;
} & CardProps;

export const ClientReplyEventCard = ({
  event,
  ...otherProps
}: ClientReplyEventCardProps) => {
  return (
    <Card p={2} {...otherProps}>
      <NodeStateNavLink
        label={`${event.eventId}: Client Reply from ${event.senderId} to ${event.recipientId}`}
        data={event}
      />
    </Card>
  );
};
