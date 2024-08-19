import { NodeStateNavLink } from "@/components/NodeStateNavLink";
import { FaultInjectionEvent } from "@/lib/byzzbench-client";
import { Card, CardProps } from "@mantine/core";
import React from "react";

export type FaultInjectionEventCardProps = {
  event: FaultInjectionEvent;
} & CardProps;

export const FaultInjectionEventCard = ({
  event,
  ...otherProps
}: FaultInjectionEventCardProps) => {
  return (
    <Card p={2} {...otherProps}>
      <NodeStateNavLink label={`${event.eventId}: Inject Fault`} data={event} />
    </Card>
  );
};
