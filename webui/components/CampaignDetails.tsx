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
      {data?.data.scheduleIds.map((scheduleId) => (
        <ScheduleCard key={scheduleId} scheduleId={scheduleId} />
      ))}
      {false && (
        <JsonInput
          value={JSON.stringify(data?.data, null, 2)}
          readOnly
          autosize
        />
      )}
    </Stack>
  );
};
