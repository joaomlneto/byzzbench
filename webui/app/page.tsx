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
import {
  Anchor,
  Container,
  Pagination,
  SimpleGrid,
  Stack,
  Title,
} from "@mantine/core";
import { usePagination } from "@mantine/hooks";
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

  // Client-side pagination for scenarios
  const SCENARIOS_PER_PAGE = 3;
  const scenariosList = scenarioIds?.data ?? [];
  const scenariosTotalPages =
    Math.ceil(scenariosList.length / SCENARIOS_PER_PAGE) || 1;
  const scenariosPagination = usePagination({
    total: scenariosTotalPages,
    initialPage: 1,
  });
  const scenariosStart = (scenariosPagination.active - 1) * SCENARIOS_PER_PAGE;
  const scenariosEnd = scenariosStart + SCENARIOS_PER_PAGE;

  // Client-side pagination for schedules
  const SCHEDULES_PER_PAGE = 3;
  const schedulesList = scheduleIds?.data ?? [];
  const schedulesTotalPages =
    Math.ceil(schedulesList.length / SCHEDULES_PER_PAGE) || 1;
  const schedulesPagination = usePagination({
    total: schedulesTotalPages,
    initialPage: 1,
  });
  const schedulesStart = (schedulesPagination.active - 1) * SCHEDULES_PER_PAGE;
  const schedulesEnd = schedulesStart + SCHEDULES_PER_PAGE;

  return (
    <Container fluid p="xl">
      <SimpleGrid cols={2}>
        <div>
          <Title order={3}>Campaigns</Title>
          <ul>
            {campaignIds?.data.map((campaignId) => (
              <li key={campaignId}>
                <Anchor component={Link} href={`/campaigns/${campaignId}`}>
                  <CampaignNameText span campaignId={campaignId} /> (
                  {campaignId})
                </Anchor>
                <CampaignProgress campaignId={Number(campaignId)} />
              </li>
            ))}
          </ul>
        </div>
        <div>
          <Title order={3}>Scenarios</Title>
          {scenariosTotalPages > 1 && (
            <Pagination
              size="sm"
              total={scenariosTotalPages}
              onChange={scenariosPagination.setPage}
              siblings={2}
              boundaries={1}
              mt="xs"
              mb="xs"
            />
          )}
          <ul>
            {scenariosList
              .slice(scenariosStart, scenariosEnd)
              .map((scenarioId) => (
                <li key={scenarioId}>
                  <Anchor component={Link} href={`/scenarios/${scenarioId}`}>
                    Scenario {scenarioId}
                  </Anchor>
                </li>
              ))}
          </ul>
        </div>
        <div>
          <Title order={3}>Schedules</Title>
          {schedulesTotalPages > 1 && (
            <Pagination
              size="sm"
              total={schedulesTotalPages}
              onChange={schedulesPagination.setPage}
              siblings={2}
              boundaries={1}
              mt="xs"
              mb="xs"
            />
          )}
          <Stack mt="md" gap="xs">
            {schedulesList
              .slice(schedulesStart, schedulesEnd)
              .map((scheduleId) => (
                <ScheduleCard
                  key={scheduleId}
                  scheduleId={Number(scheduleId)}
                />
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
