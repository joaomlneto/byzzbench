import { ClientReplyEventCard } from "@/components/Events/EventCard/ClientReplyEventCard";
import { GenericFaultEventCard } from "@/components/Events/EventCard/GenericFaultEventCard";
import { Event } from "@/lib/byzzbench-client";
import { CardProps } from "@mantine/core";
import React from "react";
import { ClientRequestEventCard } from "./ClientRequestEventCard";
import { DeliverMessageEventCard } from "./DeliverMessageCard";
import { MutateMessageEventCard } from "./MutateMessageEventCard";
import { TimeoutEventCard } from "./TimeoutEventCard";

export type EventCardProps = { event: Event } & CardProps;

export const EventCard = ({ event, ...otherProps }: EventCardProps) => {
  switch (event.type) {
    case "Message":
      return <DeliverMessageEventCard event={event} {...otherProps} />;
    case "Timeout":
      return <TimeoutEventCard event={event} {...otherProps} />;
    case "ClientRequest":
      return <ClientRequestEventCard event={event} {...otherProps} />;
    case "ClientReply":
      return <ClientReplyEventCard event={event} {...otherProps} />;
    case "GenericFault":
      return <GenericFaultEventCard event={event} {...otherProps} />;
    case "MutateMessage":
      return <MutateMessageEventCard event={event} {...otherProps} />;
    default:
      return <div>Unknown event type: {event.type}</div>;
  }
};
