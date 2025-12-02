"use client";

import { ActionCard } from "@/components/Events/EventCard";
import { Schedule } from "@/lib/byzzbench-client";
import {
  Badge,
  Card,
  Group,
  JsonInput,
  Pagination,
  Switch,
  Title,
  Tooltip,
} from "@mantine/core";
import { usePagination } from "@mantine/hooks";
import React, { useMemo, useState } from "react";
import { ScheduleMenu } from "./ScheduleMenu";

export type ScheduleDetailsProps = {
  title: string;
  schedule: Schedule;
  hideTitle?: boolean;
  hideScenario?: boolean;
  hideSchedule?: boolean;
  hideParameters?: boolean;
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
  hideParameters = false,
  entriesPerPage = ENTRIES_PER_PAGE,
  schedule,
}: ScheduleDetailsProps) => {
  const [hideDelivered, setHideDelivered] = useState(false);

  const actions = useMemo(() => {
    if (hideDelivered) {
      return schedule.actions?.filter(
        (action) => action.type !== "DeliverMessageAction",
      );
    }
    return schedule.actions;
  }, [hideDelivered, schedule.actions]);

  const numPages = Math.ceil((actions?.length ?? 0) / entriesPerPage);
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
              {schedule.scheduleId} - {title}
            </Title>
          )}
          <Switch
            checked={hideDelivered}
            onChange={(e) => setHideDelivered(e.currentTarget.checked)}
            label={"hide messages"}
          />
          {!hideScenario && (
            <>
              <Tooltip label="Length of the schedule">
                <Badge variant="white">{actions?.length}</Badge>
              </Tooltip>
              {schedule.brokenInvariants?.map((invariant) => (
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
        {!hideParameters && (
          <JsonInput
            label="Scenario Parameters"
            value={JSON.stringify(schedule.parameters, null, 2)}
            autosize
          />
        )}
        {!hideSchedule && (
          <>
            <Pagination
              total={numPages}
              onChange={pagination.setPage}
              siblings={3}
              boundaries={2}
            />
            {actions
              ?.slice(start, end)
              .map((action, i) => <ActionCard action={action} key={i} />)}
          </>
        )}
      </Card.Section>
    </Card>
  );
};
