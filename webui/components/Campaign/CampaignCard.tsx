"use client";

import { useGetCampaign } from "@/lib/byzzbench-client";
import {
  Anchor,
  Card,
  CardProps,
  Group,
  RingProgress,
  Text,
  Title,
  Tooltip,
} from "@mantine/core";
import Link from "next/link";
import React from "react";

export type CampaignCardProps = {
  campaignId: number;
} & CardProps;

export const CampaignCard = ({
  campaignId,
  ...otherProps
}: CampaignCardProps) => {
  const { data } = useGetCampaign(campaignId);

  const createdOn = data?.data.createdAt
    ? new Date(Number(data.data.createdAt) * 1000)
    : null;

  const numMissingScenarios = Math.max(
    (data?.data.numScenarios ?? 0) - (data?.data.scheduleIds.length ?? 0),
    0,
  );

  const progress =
    ((data?.data.scheduleIds.length ?? 0) / (data?.data.numScenarios ?? 1)) *
    100;

  return (
    <Card withBorder p="md" {...otherProps}>
      <Card.Section>
        <Group justify="space-between">
          <div>
            <Title order={4}>
              <Anchor component={Link} href={`/campaigns/${campaignId}`}>
                Campaign #{data?.data.campaignId}
              </Anchor>
            </Title>
            {createdOn && (
              <Text c="dimmed">Created on {createdOn.toUTCString()}</Text>
            )}
          </div>
          <Tooltip
            label={
              <div>
                <p>Total scenarios: {data?.data.numScenarios}</p>
                <p>Completed: {data?.data.scheduleIds.length}</p>
                <p>Remaining: {numMissingScenarios}</p>
              </div>
            }
          >
            <RingProgress
              thickness={6}
              sections={[{ value: progress, color: "green" }]}
              size={40}
            />
          </Tooltip>
        </Group>
      </Card.Section>
    </Card>
  );
};
