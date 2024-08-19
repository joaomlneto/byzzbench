import { NodeStateNavLink } from "@/components/NodeStateNavLink";
import { MutateMessageEvent } from "@/lib/byzzbench-client";
import { Card, CardProps } from "@mantine/core";
import React from "react";

export type MutateMessageEventCardProps = {
  event: MutateMessageEvent;
} & CardProps;

export const MutateMessageEventCard = ({
  event,
  ...otherProps
}: MutateMessageEventCardProps) => {
  return (
    <Card p={2} {...otherProps}>
      <NodeStateNavLink
        label={`${event.eventId}: Mutate Message`}
        data={event}
      />
    </Card>
  );
};
