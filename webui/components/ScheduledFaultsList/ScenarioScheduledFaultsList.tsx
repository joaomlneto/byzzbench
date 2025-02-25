"use client";
import { useGetAutomaticFaults } from "@/lib/byzzbench-client";
import React from "react";
import { ScheduledFaultsList } from "./ScheduledFaultsList";

export const ScenarioScheduledFaultsList = () => {
  const faultsQuery = useGetAutomaticFaults();
  return <ScheduledFaultsList faultIds={faultsQuery.data?.data ?? []} />;
};
