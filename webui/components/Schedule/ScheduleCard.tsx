"use client";

import { useGetSchedule } from "@/lib/byzzbench-client";
import {
  Anchor,
  Badge,
  Card,
  Group,
  NumberFormatter,
  Popover,
  Tooltip,
} from "@mantine/core";
import {
  IconBolt,
  IconBug,
  IconCircleCheck,
  IconClock,
  IconMessage,
} from "@tabler/icons-react";
import React, { useMemo, useState } from "react";

export type ScheduleCardProps = {
  scheduleId: number;
  /**
   * When true, this card will not render if the schedule is correct (i.e., not buggy).
   */
  hideIfCorrect?: boolean;
};

export const ScheduleCard = ({
  scheduleId,
  hideIfCorrect,
}: ScheduleCardProps) => {
  const { data } = useGetSchedule(scheduleId);
  const [showFaults, setShowFaults] = useState(false);

  const deliverMessageActions = useMemo(
    () =>
      (data?.data.actions ?? []).filter(
        (action) => action.type === "DeliverMessageAction",
      ),
    [data?.data.actions],
  );

  const triggerTimeoutActions = useMemo(
    () =>
      (data?.data.actions ?? []).filter(
        (action) => action.type === "TriggerTimeoutAction",
      ),
    [data?.data.actions],
  );

  const faultActions = useMemo(
    () =>
      (data?.data.actions ?? []).filter(
        (action) =>
          action.type !== "DeliverMessageAction" &&
          action.type !== "TriggerTimeoutAction",
      ),
    [data?.data.actions],
  );

  const isBuggy = (data?.data.brokenInvariants ?? []).length > 0;

  // Optionally hide correct schedules
  if (hideIfCorrect && !isBuggy) return null;

  return (
    <Card withBorder>
      <Card.Section p="xs">
        <Group justify="space-between">
          <Anchor href={"/schedules/" + scheduleId}>
            Schedule {scheduleId}
          </Anchor>
          <Group>
            <Tooltip label="Messages delivered">
              <Badge>
                <Group gap="xs">
                  <IconMessage size={12} />
                  <NumberFormatter value={deliverMessageActions.length} />
                </Group>
              </Badge>
            </Tooltip>
            <Tooltip label="Timeouts triggered">
              <Badge color="blue">
                <Group gap="xs">
                  <IconClock size={12} />
                  <NumberFormatter value={triggerTimeoutActions.length} />
                </Group>
              </Badge>
            </Tooltip>
            <Popover withArrow shadow="md">
              <Popover.Target>
                <Tooltip
                  label={
                    showFaults ? (
                      <div style={{ maxHeight: 240, overflowY: "auto" }}>
                        {faultActions.map((action, idx) => {
                          const base = `${idx + 1}. ${action.type}`;
                          // Best-effort details for known fault types
                          if (
                            action.type === "FaultInjectionAction" &&
                            (action as any).mutatorId
                          ) {
                            const m = (action as any).mutatorId as string;
                            const mid = (action as any).messageId as
                              | number
                              | undefined;
                            return (
                              <div key={idx}>
                                {base} – mutator: {m}
                                {mid != null ? ` (message ${mid})` : ""}
                              </div>
                            );
                          }
                          if (
                            action.type === "DropMessageAction" &&
                            (action as any).messageId != null
                          ) {
                            return (
                              <div key={idx}>
                                {base} – message {(action as any).messageId}
                              </div>
                            );
                          }
                          return <div key={idx}>{base}</div>;
                        })}
                      </div>
                    ) : (
                      "Faults injected"
                    )
                  }
                  withArrow
                  withinPortal
                >
                  <Badge
                    color="yellow"
                    onMouseEnter={() => setShowFaults(true)}
                    onMouseLeave={() => setShowFaults(false)}
                  >
                    <Group gap="xs">
                      <IconBolt size={12} />
                      <NumberFormatter
                        value={
                          data?.data.actions.filter(
                            (action) =>
                              action.type !== "DeliverMessageAction" &&
                              action.type !== "TriggerTimeoutAction",
                          ).length
                        }
                      />
                    </Group>
                  </Badge>
                </Tooltip>
              </Popover.Target>
              <Popover.Dropdown>
                {faultActions.map((action, idx) => {
                  const base = `${idx + 1}. ${action.type}`;
                  // Best-effort details for known fault types
                  if (
                    action.type === "FaultInjectionAction" &&
                    (action as any).mutatorId
                  ) {
                    const m = (action as any).mutatorId as string;
                    const mid = (action as any).messageId as number | undefined;
                    return (
                      <div key={idx}>
                        {base} – mutator: {m}
                        {mid != null ? ` (message ${mid})` : ""}
                      </div>
                    );
                  }
                  if (
                    action.type === "DropMessageAction" &&
                    (action as any).messageId != null
                  ) {
                    return (
                      <div key={idx}>
                        {base} – message {(action as any).messageId}
                      </div>
                    );
                  }
                  return <div key={idx}>{base}</div>;
                })}
              </Popover.Dropdown>
            </Popover>
            {!isBuggy && (
              <div>
                <Tooltip label="Correct schedule">
                  <IconCircleCheck color="green" />
                </Tooltip>
              </div>
            )}
            {isBuggy && (
              <div>
                <Tooltip
                  label={
                    <div style={{ maxHeight: 240, overflowY: "auto" }}>
                      {(data?.data.brokenInvariants ?? []).map((inv, idx) => (
                        <div key={inv.id ?? idx}>
                          {idx + 1}. {inv.id ?? "Invariant"}
                          {inv.explanation ? ` – ${inv.explanation}` : ""}
                        </div>
                      ))}
                    </div>
                  }
                  withArrow
                  withinPortal
                >
                  <IconBug color="red" />
                </Tooltip>
              </div>
            )}
          </Group>
        </Group>
      </Card.Section>
    </Card>
  );
};
