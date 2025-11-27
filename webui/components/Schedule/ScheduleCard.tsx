"use client";

import { useGetSchedule } from "@/lib/byzzbench-client";
import {
  Anchor,
  Badge,
  Card,
  Group,
  NumberFormatter,
  Tooltip,
} from "@mantine/core";
import {
  IconBolt,
  IconBug,
  IconCircleCheck,
  IconClock,
  IconMessage,
} from "@tabler/icons-react";

export type ScheduleCardProps = {
  scheduleId: number;
  /**
   * When true, this card will not render if the schedule is correct (i.e., not buggy).
   */
  hideIfCorrect?: boolean;
};

export const ScheduleCard = ({ scheduleId, hideIfCorrect }: ScheduleCardProps) => {
  const { data } = useGetSchedule(scheduleId);

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
                  <NumberFormatter
                    value={
                      data?.data.actions.filter(
                        (action) => action.type == "DeliverMessageAction",
                      ).length
                    }
                  />
                </Group>
              </Badge>
            </Tooltip>
            <Tooltip label="Timeouts triggered">
              <Badge color="blue">
                <Group gap="xs">
                  <IconClock size={12} />
                  <NumberFormatter
                    value={
                      data?.data.actions.filter(
                        (action) => action.type == "TriggerTimeoutAction",
                      ).length
                    }
                  />
                </Group>
              </Badge>
            </Tooltip>
            <Tooltip label="Faults injected">
              <Badge color="yellow">
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
            {!isBuggy && (
              <div>
                <Tooltip label="Correct schedule">
                  <IconCircleCheck color="green" />
                </Tooltip>
              </div>
            )}
            {isBuggy && (
              <div>
                <Tooltip label="Buggy schedule">
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
