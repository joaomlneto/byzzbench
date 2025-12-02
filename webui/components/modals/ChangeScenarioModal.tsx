"use client";

import { useGetScenarios } from "@/lib/byzzbench-client";
import { Button, Select, Stack } from "@mantine/core";
import { useForm } from "@mantine/form";
import { type ContextModalProps } from "@mantine/modals";
import { showNotification } from "@mantine/notifications";
import { useQueryClient } from "@tanstack/react-query";
import React from "react";

export function ChangeScenarioModal({ innerProps }: ContextModalProps<{}>) {
  const queryClient = useQueryClient();
  const scenarios = useGetScenarios();

  const form = useForm<{ scenario: string; params: string }>({
    initialValues: {
      scenario: "",
      params: "",
    },

    validate: {
      scenario: (value) => (value.length < 1 ? "Scenario ID is empty" : null),
    },

    clearInputErrorOnChange: true,
  });

  /*
  if (!form.values.scenario && currentScenarioId.data?.data) {
    form.setFieldValue("scenario", currentScenarioId.data.data);
  }

  if (!form.values.params && scenarioQuery.data?.data) {
    form.setFieldValue(
      "params",
      JSON.stringify(scenarioQuery.data.data, null, 2),
    );
  }*/

  return (
    <form
      onSubmit={form.onSubmit((values) => {
        const { scenario, params } = values;
        if (!form.isValid) {
          showNotification({ message: "Errors in form", color: "red" });
          return;
        }

        // try parsing the params
        let parsedParams;
        try {
          parsedParams = JSON.parse(params);
        } catch (error) {
          showNotification({ message: "Invalid JSON", color: "red" });
          return;
        }

        console.log("Changing scenario", values);
        showNotification({
          message: `No longer available after rework`,
        });
        /*
        void changeScenario({ scenarioId: scenario }, parsedParams)
          .then(async () => {
            console.log("Scenario changed to ", scenario);
          })
          .catch((error) => {
            console.log("Failed to change scenario", error);
          })
          .finally(() => {
            queryClient.invalidateQueries();
            modals.closeAll();
          });*/
      })}
    >
      <Stack gap="sm">
        <Select
          data={/*scenarios.data?.data*/ []}
          value={form.values.scenario}
          onChange={(value) => form.setFieldValue("scenario", value ?? "")}
        />
        <Button type="submit">Change Scenario</Button>
      </Stack>
    </form>
  );
}
