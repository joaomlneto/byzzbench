import { ActionListEntry } from "@/components/Action/ActionListEntry";
import { useGetScenarioAvailableActions } from "@/lib/byzzbench-client";

export type ScenarioActionListProps = {
  scenarioId: number;
};

export const ScenarioActionList = ({ scenarioId }: ScenarioActionListProps) => {
  const { data } = useGetScenarioAvailableActions(scenarioId);

  return (
    <div>
      <ul>
        {data?.data.map((action, actionId) => (
          <li key={actionId}>
            <div>
              <ActionListEntry
                scenarioId={scenarioId}
                action={action}
                actionId={actionId}
                explorationStrategyId={""}
              />
            </div>
          </li>
        ))}
      </ul>
    </div>
  );
};
