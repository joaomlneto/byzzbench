"use client";

import { EventCard } from "@/components/Events/EventCard";
import {
  changeScenario,
  deliverMessage,
  enableNetworkFault,
  GenericFaultEvent,
  getEvent,
  mutateMessage,
  MutateMessageEvent,
  Schedule,
} from "@/lib/byzzbench-client";
import { Button, Container, Group, Title } from "@mantine/core";
import { openContextModal } from "@mantine/modals";
import { showNotification } from "@mantine/notifications";
import { useQueryClient } from "@tanstack/react-query";
import { useRouter } from "next/navigation";
import React from "react";

export type ScheduleDetailsProps = {
  title: string;
  schedule: Schedule;
  hideTitle?: boolean;
  hideScenario?: boolean;
  hideSchedule?: boolean;
  hideMaterializeButton?: boolean;
  hideSaveButton?: boolean;
  hideDownloadButton?: boolean;
  hideDetailsButton?: boolean;
};

export const ScheduleDetails = ({
  title,
  hideTitle = false,
  hideScenario = false,
  hideSchedule = false,
  hideMaterializeButton = false,
  hideSaveButton = false,
  hideDownloadButton = false,
  hideDetailsButton = false,
  schedule,
}: ScheduleDetailsProps) => {
  const queryClient = useQueryClient();
  const router = useRouter();

  return (
    <Container size="sm">
      {!hideTitle && <Title order={4}>{title}</Title>}
      {!hideScenario && (
        <Title order={6}>Scenario: {schedule.scenarioId}</Title>
      )}
      <Group gap="xs">
        {!hideMaterializeButton && (
          <Button
            size="xs"
            onClick={async () => {
              console.log("Materializing Schedule: ", schedule);
              await changeScenario({ scenarioId: schedule.scenarioId });
              let i = 0;
              let hasNotifiedMismatchedEvents = false;

              for (const event of schedule.events ?? []) {
                const remoteEvent = await getEvent(event.eventId).then(
                  (event) => event.data,
                );

                // check if events do not match; if so, notify and break
                // check if event and remoteEvent are the same
                const objA = { ...event, createdAt: null, status: null };
                const objB = {
                  ...remoteEvent,
                  createdAt: null,
                  status: null,
                };
                if (JSON.stringify(objA) !== JSON.stringify(objB)) {
                  hasNotifiedMismatchedEvents = true;
                  console.error(
                    `Event ${event.eventId} does not match remote event`,
                    objA,
                    objB,
                  );
                  if (!hasNotifiedMismatchedEvents) {
                    showNotification({
                      title: "Error materializing schedule",
                      message: `Event ${event.eventId} does not match remote event. Will continue to try to materialize the rest of the schedule. See console for more details.`,
                      color: "red",
                    });
                  }
                  //break;
                  //continue;
                }

                console.log(
                  `Pushing Event ${++i}/${schedule.events.length}: ${event}`,
                );
                switch (event.type) {
                  case "Message":
                  case "Timeout":
                  case "ClientRequest":
                    await deliverMessage(event.eventId);
                    break;
                  case "MutateMessage":
                    await mutateMessage(
                      (event as MutateMessageEvent).payload!.eventId,
                      (event as MutateMessageEvent).payload!.mutatorId,
                    );
                    break;
                  case "GenericFault":
                    await enableNetworkFault(
                      (event as GenericFaultEvent).payload!.id!,
                    );
                  default:
                    console.error("Unknown event type", event);
                }
              }
              await queryClient.invalidateQueries();
              router.push("/");
            }}
          >
            Materialize
          </Button>
        )}
        {!hideSaveButton && (
          <Button
            size="xs"
            onClick={(e) => {
              e.preventDefault();
              openContextModal({
                modal: "saveSchedule",
                title: <Title order={3}>Save Schedule</Title>,
                size: "xl",
                centered: true,
                innerProps: {
                  name: title,
                  schedule: schedule,
                },
              });
            }}
          >
            Save
          </Button>
        )}
        {!hideDownloadButton && (
          <Button
            size="xs"
            onClick={(e) => {
              e.preventDefault();
              const jsonString = `data:text/json;charset=utf-8,${encodeURIComponent(
                JSON.stringify(schedule),
              )}`;
              const link = document.createElement("a");
              link.href = jsonString;
              link.download = `${schedule.scenarioId}-${Date.now()}.schedule.json`;
              link.click();
            }}
          >
            Download
          </Button>
        )}
        {!hideDetailsButton && (
          <Button
            size="xs"
            onClick={(e) => {
              e.preventDefault();
              openContextModal({
                title: "Schedule Details",
                modal: "scheduleDetails",
                size: "md",
                innerProps: {
                  title: schedule.scenarioId,
                  schedule: schedule,
                },
              });
            }}
          >
            Show Details
          </Button>
        )}
      </Group>
      {!hideSchedule &&
        schedule.events?.map((event) => (
          <EventCard event={event} key={event.eventId} />
        ))}
    </Container>
  );
};
