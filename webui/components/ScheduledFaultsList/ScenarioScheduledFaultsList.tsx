"use client";
import { useGetAutomaticFaults } from "@/lib/byzzbench-client";
import React from "react";
import { ScheduledFaultsList } from "./ScheduledFaultsList";

export type ScenarioScheduledFaultsListProps = {
  scenarioId: number;
};

export const ScenarioScheduledFaultsList = ({
  scenarioId,
}: ScenarioScheduledFaultsListProps) => {
  const faultsQuery = useGetAutomaticFaults(scenarioId);
  return (
    <ScheduledFaultsList
      scenarioId={scenarioId}
      faultIds={faultsQuery.data?.data ?? []}
    />
  );
};
