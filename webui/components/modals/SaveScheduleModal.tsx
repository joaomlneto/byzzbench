"use client";

import { ScheduleDetails } from "@/components/ScheduleDetails";
import { Action, useSavedSchedulesStore } from "@/hooks/useSavedSchedules";
import { getMessage, getSchedule } from "@/lib/byzzbench-client";
import { Button, JsonInput, Stack, TextInput } from "@mantine/core";
import { useForm } from "@mantine/form";
import { type ContextModalProps, modals } from "@mantine/modals";
import React, { useEffect } from "react";

export function SaveScheduleModal({
  innerProps,
}: ContextModalProps<{ actions: Action[] }>) {
  const actions = innerProps.actions;

  const form = useForm<{ name: string; events: Action[] }>({
    initialValues: {
      name: "",
      events: [],
    },

    validate: {
      name: (value) => {
        return value.length < 1 ? "Name is required" : null;
      },
      events: (value) => {
        return value.length < 1 ? "No events" : null;
      },
    },

    clearInputErrorOnChange: true,
  });

  const addSchedule = useSavedSchedulesStore((x) => x.addSchedule);

  useEffect(() => {
    const fetchData = async () => {
      // get event ids
      const { data: eventIds } = await getSchedule();

      // get details for every event
      const events = await Promise.all(
        eventIds.map(async (eventId) => {
          const { data: event } = await getMessage(eventId);
          return event;
        }),
      );

      const actions = events.map((event) => ({
        type: "DeliverEvent",
        label: `Deliver event #${event.eventId}`,
        event,
      })) satisfies Action[];

      form.setFieldValue("events", actions);
    };

    void fetchData();
  }, []);

  return (
    <form
      onSubmit={form.onSubmit((values) => {
        console.log("Adding schedule", values);
        addSchedule({
          name: values.name,
          actions: values.events,
        });
        modals.closeAll();
      })}
    >
      <Stack gap="sm">
        <TextInput label="Trace Name" {...form.getInputProps("name")} />
        {false && (
          <JsonInput
            value={JSON.stringify(form.getValues().events, null, 2)}
            autosize
            maxRows={20}
          />
        )}
        <ScheduleDetails
          schedule={{ name: "Schedule", actions: form.getValues().events }}
        />
        <Button type="submit">Save</Button>
      </Stack>
    </form>
  );
}
