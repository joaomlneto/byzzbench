import { PerformActionActionIcon } from "@/components/Action/PerformActionActionIcon";
import { FaultInjectionAction } from "@/lib/byzzbench-client";
import { JsonInput } from "@mantine/core";
import { IconBolt } from "@tabler/icons-react";
import React from "react";

export type FaultInjectionActionListEntryProps = {
  scenarioId: number;
  action: FaultInjectionAction;
  actionId: number;
  explorationStrategyId: string;
};

export const FaultInjectionActionListEntry = ({
  scenarioId,
  action,
  actionId,
  explorationStrategyId,
}: FaultInjectionActionListEntryProps) => {
  return (
    <div>
      <IconBolt size={16} /> {action.faultBehaviorId} ({action.eventId})
      <PerformActionActionIcon
        scenarioId={scenarioId}
        actionId={actionId}
        explorationStrategyId={explorationStrategyId}
      />
      <JsonInput
        readOnly
        autosize
        maxRows={10}
        //        value={JSON.stringify(action.payload, null, 2)}
        value={JSON.stringify(action, null, 2)}
      />
    </div>
  );
};
