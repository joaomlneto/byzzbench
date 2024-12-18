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
import { ActionIcon, Burger, Menu, Title } from "@mantine/core";
import { openContextModal } from "@mantine/modals";
import { showNotification } from "@mantine/notifications";
import { useQueryClient } from "@tanstack/react-query";
import { useRouter } from "next/navigation";
import React from "react";

export type ScheduleMenuProps = {
  title: string;
  schedule: Schedule;
};

export const ScheduleMenu = ({ title, schedule }: ScheduleMenuProps) => {
  const queryClient = useQueryClient();
  const router = useRouter();
  return (
    <Menu>
      <Menu.Target>
        <ActionIcon variant="subtle">
          <Burger size={16} />
        </ActionIcon>
      </Menu.Target>
      <Menu.Dropdown>
        <Menu.Item
          onClick={async () => {
            console.log("Materializing Schedule: ", schedule);
            await changeScenario({ scenarioId: schedule.scenarioId }, {});
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
                  break;
                default:
                  console.error("Unknown event type", event);
              }
            }
            await queryClient.invalidateQueries();
            router.push("/");
          }}
        >
          Materialize
        </Menu.Item>
        <Menu.Item
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
        </Menu.Item>
        <Menu.Item
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
        </Menu.Item>
        <Menu.Item
          onClick={(e) => {
            e.preventDefault();
            openContextModal({
              title: "Schedule Details",
              modal: "scheduleDetails",
              size: "md",
              innerProps: {
                title: title,
                schedule: schedule,
              },
            });
          }}
        >
          Show Details
        </Menu.Item>
      </Menu.Dropdown>
    </Menu>
  );
};
