"use client";

import { EventCard } from "@/components/Events/EventCard";
import { Schedule } from "@/lib/byzzbench-client";
import { Badge, Card, Group, Pagination, Title, Tooltip } from "@mantine/core";
import { usePagination } from "@mantine/hooks";
import { useQueryClient } from "@tanstack/react-query";
import { useRouter } from "next/navigation";
import React from "react";
import { ScheduleMenu } from "./ScheduleMenu";

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
  entriesPerPage?: number;
};

const ENTRIES_PER_PAGE = 50;

export const ScheduleDetails = ({
  title,
  hideTitle = false,
  hideScenario = false,
  hideSchedule = false,
  entriesPerPage = ENTRIES_PER_PAGE,
  schedule,
}: ScheduleDetailsProps) => {
  const queryClient = useQueryClient();
  const router = useRouter();
  const numPages = Math.ceil((schedule.events?.length ?? 0) / entriesPerPage);
  const pagination = usePagination({ total: numPages, initialPage: 1 });
  const start = (pagination.active - 1) * entriesPerPage;
  const end = start + entriesPerPage;

  return (
    <Card>
      <Card.Section>
        <Group>
          <ScheduleMenu title={title} schedule={schedule} />
          {!hideTitle && (
            <Title order={4}>
              {schedule.scenarioId} - {title}
            </Title>
          )}
          {!hideScenario && (
            <>
              <Tooltip label="Length of the schedule">
                <Badge variant="white">{schedule.events.length}</Badge>
              </Tooltip>
              {schedule.brokenInvariants.map((invariant) => (
                <Tooltip
                  key={invariant.id}
                  label={`Broken invariant: ${invariant.id}`}
                >
                  <Badge key={invariant.id} size="xs" color="red">
                    {invariant.id}
                  </Badge>
                </Tooltip>
              ))}
            </>
          )}
        </Group>
        {!hideSchedule && (
          <>
            <Pagination
              total={numPages}
              onChange={pagination.setPage}
              siblings={3}
              boundaries={2}
            />
            {schedule.events
              ?.slice(start, end)
              .map((event) => <EventCard event={event} key={event.eventId} />)}
          </>
        )}
      </Card.Section>
    </Card>
  );
};
