"use client";

import {
  changeScenario,
  useGetCurrentScenarioId,
  useGetScenarios,
} from "@/lib/byzzbench-client";
import { Button, JsonInput, Select, Stack } from "@mantine/core";
import { useForm } from "@mantine/form";
import { type ContextModalProps, modals } from "@mantine/modals";
import { showNotification } from "@mantine/notifications";
import { useQueryClient } from "@tanstack/react-query";
import React from "react";

const defaultParams = {
  scenario: {
    numClients: 1,
  },
  faults: {
    process: [],
    network: [],
    event: [],
  },
  scheduler: {
    type: "random",
    params: {
      probabilities: {
        deliverEvent: 0.9,
        dropMessage: 0.1,
        mutateMessage: 0.0,
      },
    },
  },
};

export function ChangeScenarioModal({ innerProps }: ContextModalProps<{}>) {
  const queryClient = useQueryClient();
  const scenarios = useGetScenarios();
  const currentScenarioId = useGetCurrentScenarioId();

  const form = useForm<{ scenario: string; params: string }>({
    initialValues: {
      scenario: currentScenarioId.data?.data ?? "",
      params: JSON.stringify(defaultParams, null, 2),
    },

    validate: {
      scenario: (value) => (value.length < 1 ? "Scenario ID is empty" : null),
    },

    clearInputErrorOnChange: true,
  });

  if (!form.values.scenario && currentScenarioId.data?.data) {
    form.setFieldValue("scenario", currentScenarioId.data.data);
  }

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
          });
      })}
    >
      <Stack gap="sm">
        <Select
          data={scenarios.data?.data}
          value={form.values.scenario}
          onChange={(value) => form.setFieldValue("scenario", value ?? "")}
        />
        <JsonInput
          label="Params"
          placeholder="Params"
          minRows={10}
          maxRows={30}
          autosize
          formatOnBlur
          value={form.values.params}
          onChange={(value) => form.setFieldValue("params", value)}
        />
        <Button type="submit">Change Scenario</Button>
      </Stack>
    </form>
  );
}
