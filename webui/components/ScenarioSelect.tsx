"use client";

import { useGetScenarios } from "@/lib/byzzbench-client";
import { Select } from "@mantine/core";
import { showNotification } from "@mantine/notifications";
import { useQueryClient } from "@tanstack/react-query";
import React from "react";

export const ScenarioSelect = () => {
  const queryClient = useQueryClient();
  const scenarios = useGetScenarios();
  return (
    <Select
      data={scenarios.data?.data}
      //value={currentScenarioId.data?.data}
      onChange={(value) => {
        // check if value is null
        if (value === null) {
          console.log("Scenario is null");
          return;
        }
        console.log("Selected scenario", value);
        showNotification({
          message: "No longer available",
        });
        /*
        void changeScenario(
          { scenarioId: value },
        )
        .then(async () => {
          queryClient.invalidateQueries();
          console.log("Scenario changed to ", value);
        })
        .catch((error) => {
          console.log("Failed to change scenario", error);
        });*/
      }}
    />
  );
};
