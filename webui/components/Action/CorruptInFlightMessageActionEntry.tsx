import { PerformActionActionIcon } from "@/components/Action/PerformActionActionIcon";
import { FaultInjectionAction } from "@/lib/byzzbench-client";
import { JsonInput } from "@mantine/core";
import { IconBolt } from "@tabler/icons-react";
import React from "react";

export type CorruptInFlightMessageActionProps = {
  scenarioId: number;
  action: FaultInjectionAction;
  actionId: number;
  explorationStrategyId: string;
};

export const CorruptInFlightMessageActionEntry = ({
  scenarioId,
  action,
  actionId,
  explorationStrategyId,
}: CorruptInFlightMessageActionProps) => {
  return (
    <div>
      <IconBolt size={16} /> {actionId} {action.type}
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
