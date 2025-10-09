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
