"use client";

import { Action } from "@/lib/byzzbench-client";
import { CardProps } from "@mantine/core";
import React from "react";
import { DeliverMessageActionCard } from "./DeliverMessageCard";
import { FaultInjectionActionCard } from "./FaultInjectionActionCard";
import { TimeoutActionCard } from "./TimeoutActionCard";

export type ActionCardProps = { action: Action } & CardProps;

export const ActionCard = ({ action, ...otherProps }: ActionCardProps) => {
  switch (action.type) {
    case "DeliverMessageAction":
      return <DeliverMessageActionCard action={action} {...otherProps} />;
    case "TriggerTimeoutAction":
      return <TimeoutActionCard action={action} {...otherProps} />;
    /*
    case "ClientRequest":
      return <ClientRequestActionCard action={action} {...otherProps} />;
    case "GenericFault":
      return <GenericFaultEventCard action={action} {...otherProps} />;
    */
    case "FaultInjectionAction":
      return <FaultInjectionActionCard action={action} {...otherProps} />;
    default:
      return <div>Unknown event type: {action.type}</div>;
  }
};
