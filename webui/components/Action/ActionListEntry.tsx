import { NodeMailboxEntry } from "@/components/Events";
import {
  DeliverMessageAction,
  FaultInjectionAction,
  TriggerTimeoutAction,
} from "@/lib/byzzbench-client";
import React from "react";

export type ActionListEntryProps = {
  scenarioId: number;
  action: DeliverMessageAction | FaultInjectionAction | TriggerTimeoutAction;
};

export const ActionListEntry = ({
  scenarioId,
  action,
}: ActionListEntryProps) => {
  if (action.type === "DeliverMessageAction") {
    const msgAction = action as DeliverMessageAction;
    return (
      <div>
        Deliver Message: {msgAction.payload?.type} from {msgAction.senderId} to{" "}
        {msgAction.recipientId}
        <NodeMailboxEntry
          scenarioId={scenarioId}
          messageId={msgAction.messageEventId!}
        />
      </div>
    );
  }

  if (action.type === "TriggerTimeoutAction") {
    const timeoutAction = action as TriggerTimeoutAction;
    return (
      <div>
        Timeout node {timeoutAction.nodeId} ({timeoutAction.description})
        <NodeMailboxEntry
          scenarioId={scenarioId}
          messageId={timeoutAction.timeoutEventId!}
        />
      </div>
    );
  }

  return (
    <div>
      {action.type} {JSON.stringify(action)}
    </div>
  );
};
