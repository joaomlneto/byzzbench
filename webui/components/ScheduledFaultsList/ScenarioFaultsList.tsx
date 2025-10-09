"use client";
import { FaultsList } from "@/components/FaultsList/FaultsList";
import { useGetNetworkFaults } from "@/lib/byzzbench-client";

export type ScenarioFaultsListProps = {
  scenarioId: number;
};

export const ScenarioFaultsList = ({ scenarioId }: ScenarioFaultsListProps) => {
  const faultsQuery = useGetNetworkFaults(scenarioId);
  return (
    <FaultsList
      scenarioId={scenarioId}
      faultIds={faultsQuery.data?.data ?? []}
    />
  );
};
