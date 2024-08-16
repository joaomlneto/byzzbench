"use client";

import {
  changeScenario,
  useGetCurrentScenarioId,
  useGetScenarios,
} from "@/lib/byzzbench-client";
import { Select } from "@mantine/core";
import { useQueryClient } from "@tanstack/react-query";
import React from "react";

export const ScenarioSelect = () => {
  const queryClient = useQueryClient();
  const scenarios = useGetScenarios();
  const currentScenarioId = useGetCurrentScenarioId();
  return (
    <Select
      data={scenarios.data?.data}
      value={currentScenarioId.data?.data}
      onChange={(value) => {
        // check if value is null
        if (value === null) {
          console.log("Scenario is null");
          return;
        }
        void changeScenario({ scenarioId: value })
          .then(async () => {
            queryClient.invalidateQueries();
            console.log("Scenario changed to ", value);
          })
          .catch((error) => {
            console.log("Failed to change scenario", error);
          });
      }}
    />
  );
};
