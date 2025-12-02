"use client";

import { useGetCampaign } from "@/lib/byzzbench-client";
import { Loader, Progress, Tooltip } from "@mantine/core";
import React from "react";

export type CampaignProgressProps = {
  campaignId: number;
  size?: number;
};

export const CampaignProgress = ({ campaignId, size = 24 }: CampaignProgressProps) => {
  const { data, isLoading } = useGetCampaign(campaignId);

  if (isLoading) return <Loader size="sm" />;
  if (!data) return null;

  const numScenarios = data.data.numScenarios ?? 0;
  const numBuggy = data.data.numTerm ?? 0;
  const numErrored = data.data.numErr ?? 0;
  const numCorrect = data.data.numMaxedOut ?? 0;

  // Avoid division by zero
  const denom = numScenarios === 0 ? 1 : numScenarios;

  const notRan = numScenarios - numBuggy - numErrored - numCorrect;

  return (
    <Progress.Root size={size} mt={4}>
      <Tooltip label={`Buggy - reached incorrect state - ${numBuggy}`}>
        <Progress.Section value={(numBuggy / denom) * 100} color="red">
          <Progress.Label>Buggy</Progress.Label>
        </Progress.Section>
      </Tooltip>

      <Tooltip label={`Errored - runtime exception - ${numErrored}`}>
        <Progress.Section value={(numErrored / denom) * 100} color="yellow">
          <Progress.Label>Errored</Progress.Label>
        </Progress.Section>
      </Tooltip>

      <Tooltip label={`Correct (no issues found before cutoff) - ${numCorrect}`}>
        <Progress.Section value={(numCorrect / denom) * 100} color="green">
          <Progress.Label>Correct ({numCorrect})</Progress.Label>
        </Progress.Section>
      </Tooltip>

      <Tooltip label={`Not yet ran - ${notRan}`}>
        <Progress.Section value={(notRan / denom) * 100} color="grey">
          <Progress.Label>Not ran ({notRan})</Progress.Label>
        </Progress.Section>
      </Tooltip>
    </Progress.Root>
  );
};
