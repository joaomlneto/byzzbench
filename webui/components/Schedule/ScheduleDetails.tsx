"use client";

import { EventCard } from "@/components/Events/EventCard";
import {
  changeScenario,
  deliverMessage,
  Schedule,
} from "@/lib/byzzbench-client";
import { Button, Container, Group, Title } from "@mantine/core";
import React from "react";

export type ScheduleDetailsProps = {
  schedule: Schedule;
  hideTitle?: boolean;
  hideSchedule?: boolean;
};

export const ScheduleDetails = ({
  hideTitle = false,
  hideSchedule = false,
  schedule,
}: ScheduleDetailsProps) => {
  return (
    <Container size="sm">
      {!hideTitle && <Title order={6}>Scenario: {schedule.scenarioId}</Title>}
      <Group>
        <Button
          onClick={async () => {
            console.log("Materializing Schedule: ", schedule);
            await changeScenario({ scenarioId: schedule.scenarioId });
            let i = 0;
            for (const event of schedule.events ?? []) {
              console.log(
                `Pushing Event ${++i}/${schedule.events.length}: ${event}`,
              );
              switch (event.type) {
                case "Message":
                case "Timeout":
                case "ClientRequest":
                  await deliverMessage(event.eventId);
                  break;
                default:
                  console.error("Unknown event type", event);
              }
            }
          }}
        >
          Materialize Schedule
        </Button>
      </Group>
      {!hideSchedule &&
        schedule.events?.map((event) => (
          <EventCard event={event} key={event.eventId} />
        ))}
    </Container>
  );
};
