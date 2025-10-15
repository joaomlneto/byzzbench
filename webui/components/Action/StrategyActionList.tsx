import { ActionListEntry } from "@/components/Action/ActionListEntry";
import { useGetStrategyAvailableActions } from "@/lib/byzzbench-client";

export type StrategyActionListProps = {
  strategyId: string;
  scenarioId: number;
};

export const StrategyActionList = ({
  strategyId,
  scenarioId,
}: StrategyActionListProps) => {
  const { data } = useGetStrategyAvailableActions(strategyId, scenarioId);

  return (
    <div>
      <ul>
        {data?.data.map((action) => (
          <li key={action.actionId}>
            <div>
              <ActionListEntry scenarioId={scenarioId} action={action} />
            </div>
          </li>
        ))}
      </ul>
    </div>
  );
};
