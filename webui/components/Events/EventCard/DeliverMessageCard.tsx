import { NodeStateNavLink } from "@/components/NodeStateNavLink";
import { MessageEvent } from "@/lib/byzzbench-client";
import { Card, CardProps } from "@mantine/core";
import React from "react";

export type DeliverMessageEventCardProps = {
  event: MessageEvent;
} & CardProps;

export const DeliverMessageEventCard = ({
  event,
  ...otherProps
}: DeliverMessageEventCardProps) => {
  return (
    <Card p={2} {...otherProps}>
      <NodeStateNavLink
        label={`${event.eventId}: Deliver ${event.payload?.type} from ${event.senderId} to ${event.recipientId}`}
        data={event}
      />
    </Card>
  );
};
