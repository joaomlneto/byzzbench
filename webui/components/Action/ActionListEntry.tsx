import { FaultInjectionActionListEntry } from "@/components/Action/FaultnjectionActionListEntry";
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
  actionId: number;
  explorationStrategyId: string;
};

export const ActionListEntry = ({
  scenarioId,
  action,
  actionId,
  explorationStrategyId,
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

  if (action.type === "FaultInjectionAction") {
    const faultInjectionAction = action as FaultInjectionAction;
    return (
      <FaultInjectionActionListEntry
        action={faultInjectionAction}
        scenarioId={scenarioId}
        actionId={actionId}
        explorationStrategyId={explorationStrategyId}
      />
    );
  }

  if (action.type === "DropMessageAction") {
    const faultInjectionAction = action as FaultInjectionAction;
    return (
      <FaultInjectionActionListEntry
        action={faultInjectionAction}
        scenarioId={scenarioId}
        actionId={actionId}
        explorationStrategyId={explorationStrategyId}
      />
    );
  }

  return (
    <div>
      {action.type} {JSON.stringify(action)}
    </div>
  );
};
