"use client";

import { ScheduleDetails } from "@/components/Schedule";
import { Action, useSavedSchedulesStore } from "@/hooks/useSavedSchedules";
import { getSchedule, Schedule } from "@/lib/byzzbench-client";
import { Button, Stack, TextInput } from "@mantine/core";
import { useForm } from "@mantine/form";
import { type ContextModalProps, modals } from "@mantine/modals";
import { showNotification } from "@mantine/notifications";
import React, { useEffect } from "react";

export function SaveScheduleModal({
  innerProps,
}: ContextModalProps<{ actions: Action[] }>) {
  const actions = innerProps.actions;

  const form = useForm<{ name: string; schedule?: Schedule }>({
    initialValues: {
      name: "",
    },

    validate: {
      name: (value) => {
        return value.length < 1 ? "Name is required" : null;
      },
    },

    clearInputErrorOnChange: true,
  });

  const addSchedule = useSavedSchedulesStore((x) => x.addSchedule);

  useEffect(() => {
    const fetchData = async () => {
      // get event ids
      const schedule = await getSchedule().then((res) => res.data);

      form.setFieldValue("events", schedule);
    };

    void fetchData();
  }, [form]);

  return (
    <form
      onSubmit={form.onSubmit((values) => {
        if (!values.schedule) {
          showNotification({ message: "No schedule to save", color: "red" });
          return;
        }
        console.log("Adding schedule", values);
        addSchedule(values.name, values.schedule);
        modals.closeAll();
      })}
    >
      <Stack gap="sm">
        <TextInput label="Trace Name" {...form.getInputProps("name")} />
        <ScheduleDetails
          schedule={form.getValues().schedule ?? { scenarioId: "", events: [] }}
        />
        <Button type="submit">Save</Button>
      </Stack>
    </form>
  );
}
