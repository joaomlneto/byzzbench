"use client";

import { ScheduleCard } from "@/components/Schedule";
import { useGetCampaign } from "@/lib/byzzbench-client";

import { JsonInput, Stack } from "@mantine/core";
import React from "react";

export type CampaignDetailsProps = {
  campaignId: number;
};

export const CampaignDetails = ({ campaignId }: CampaignDetailsProps) => {
  const { data } = useGetCampaign(campaignId);

  const createdAt = data?.data.createdAt
    ? new Date((data.data.createdAt as unknown as number) * 1000).toUTCString()
    : "N/A";

  return (
    <Stack gap="xs">
      {data?.data.createdAt && <p>Created {createdAt}</p>}
      <JsonInput
        label="Campaign Details"
        value={JSON.stringify(
          {
            ...data?.data,
            campaignId: undefined,
            scheduleIds: undefined,
            createdAt: undefined,
          },
          null,
          2,
        )}
        readOnly
        autosize
      />
      {data?.data.scheduleIds.map((scheduleId) => (
        <ScheduleCard key={scheduleId} scheduleId={scheduleId} />
      ))}
    </Stack>
  );
};
