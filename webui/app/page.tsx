"use client";

import { SchedulerAnchor } from "@/components/Anchor";
import { CampaignNameText } from "@/components/Campaign/CampaignNameText";
import { CampaignProgress } from "@/components/Campaign/CampaignProgress";
import { ScheduleCard } from "@/components/Schedule";
import {
  useGetCampaigns,
  useGetScenarios,
  useGetSchedulers,
  useGetSchedules,
} from "@/lib/byzzbench-client";
import { Anchor, Container, SimpleGrid, Stack, Title } from "@mantine/core";
import Link from "next/link";
import React from "react";

/*
const AdoBStateDiagram = dynamic<{}>(
    () =>
        import("@/components/adob/AdoBStateDiagram").then(
            (m) => m.AdoBStateDiagram,
        ),
    {
        ssr: false,
    },
);*/

export default function Home() {
  const { data: campaignIds } = useGetCampaigns();
  const { data: scenarioIds } = useGetScenarios();
  const { data: scheduleIds } = useGetSchedules();
  const { data: schedulerIds } = useGetSchedulers();

  return (
    <Container fluid p="xl">
      <SimpleGrid cols={2}>
        <div>
          <Title order={3}>Active Campaigns</Title>
          <ul>
            {campaignIds?.data.map((campaignId) => (
              <li key={campaignId}>
                <Anchor component={Link} href={`/campaigns/${campaignId}`}>
                  <CampaignNameText span campaignId={campaignId} /> ({campaignId})
                </Anchor>
                <CampaignProgress campaignId={Number(campaignId)} />
              </li>
            ))}
          </ul>
        </div>
        <div>
          <Title order={3}>Active Scenarios</Title>
          <ul>
            {scenarioIds?.data.map((scenarioId) => (
              <li key={scenarioId}>
                <Anchor component={Link} href={`/scenarios/${scenarioId}`}>
                  Scenario {scenarioId}
                </Anchor>
              </li>
            ))}
          </ul>
        </div>
        <div>
          <Title order={3}>Saved Schedules</Title>
          <Stack mt="md" gap="xs">
            {scheduleIds?.data.map((scheduleId) => (
              <ScheduleCard key={scheduleId} scheduleId={Number(scheduleId)} />
            ))}
          </Stack>
        </div>
        <div>
          <Title order={3}>Exploration Strategies</Title>
          <ul>
            {schedulerIds?.data.map((schedulerId) => (
              <li key={schedulerId}>
                <SchedulerAnchor schedulerId={schedulerId}>
                  {schedulerId}
                </SchedulerAnchor>
              </li>
            ))}
          </ul>
        </div>
      </SimpleGrid>
    </Container>
  );
}
