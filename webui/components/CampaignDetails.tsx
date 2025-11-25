"use client";

import { ScheduleCard } from "@/components/Schedule";
import { SchedulerState } from "@/components/Scheduler";
import { useGetCampaign } from "@/lib/byzzbench-client";

import {
  JsonInput,
  Loader,
  Progress,
  Stack,
  Title,
  Tooltip,
} from "@mantine/core";
import React from "react";

export type CampaignDetailsProps = {
  campaignId: number;
};

export const CampaignDetails = ({ campaignId }: CampaignDetailsProps) => {
  const { data, isLoading } = useGetCampaign(campaignId);

  if (isLoading) {
    return <Loader />;
  }

  if (!data) {
    return "Error fetching campaign data";
  }

  const createdAt = data?.data.createdAt
    ? new Date((data.data.createdAt as unknown as number) * 1000).toUTCString()
    : "N/A";

  const numScenarios = data.data.numScenarios!;
  const numBuggy = data.data.numTerm!;
  const numErrored = data.data.numErr!;
  const numCorrect = data.data.numMaxedOut!;

  return (
    <Stack gap="xs">
      <p>{data?.data.createdAt && <p>Created {createdAt}</p>}</p>
      <Progress.Root size={40}>
        <Tooltip label={`Buggy - reached incorrect state - ${numBuggy}`}>
          <Progress.Section value={(numBuggy / numScenarios) * 100} color="red">
            <Progress.Label>Buggy</Progress.Label>
          </Progress.Section>
        </Tooltip>

        <Tooltip label={`Errored - runtime exception - ${numErrored}`}>
          <Progress.Section
            value={(numErrored / numScenarios) * 100}
            color="yellow"
          >
            <Progress.Label>Errored</Progress.Label>
          </Progress.Section>
        </Tooltip>

        <Tooltip
          label={`Correct (no issues found before cutoff) - ${numCorrect}`}
        >
          <Progress.Section
            value={(numCorrect / numScenarios) * 100}
            color="green"
          >
            <Progress.Label>Correct ({numCorrect})</Progress.Label>
          </Progress.Section>
        </Tooltip>

        <Tooltip
          label={`Not yet ran - ${numScenarios - numBuggy - numErrored - numCorrect}`}
        >
          <Progress.Section
            value={
              ((numScenarios - numBuggy - numErrored - numCorrect) /
                numScenarios) *
              100
            }
            color="grey"
          >
            <Progress.Label>
              Not ran ({numScenarios - numBuggy - numErrored - numCorrect})
            </Progress.Label>
          </Progress.Section>
        </Tooltip>
      </Progress.Root>
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
      <Title order={1}>Exploration Strategy</Title>
      {data?.data.explorationStrategyInstanceId && (
        <SchedulerState
          schedulerId={data?.data.explorationStrategyInstanceId}
        />
      )}
    </Stack>
  );
};
