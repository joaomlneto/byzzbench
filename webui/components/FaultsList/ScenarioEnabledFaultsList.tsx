"use client";
import { FaultsList } from "@/components/FaultsList/FaultsList";
import { useGetEnabledNetworkFaults } from "@/lib/byzzbench-client";

export type ScenarioEnabledFaultsListProps = {
  scenarioId: number;
};

export const ScenarioEnabledFaultsList = ({
  scenarioId,
}: ScenarioEnabledFaultsListProps) => {
  const faultsQuery = useGetEnabledNetworkFaults(scenarioId);
  return (
    <FaultsList
      scenarioId={scenarioId}
      faultIds={faultsQuery.data?.data ?? []}
    />
  );
};
