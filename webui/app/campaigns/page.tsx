"use client";

import { CampaignCard } from "@/components/Campaign";
import { useGetCampaigns } from "@/lib/byzzbench-client";
import { Container, SimpleGrid, Title } from "@mantine/core";
import React from "react";

export default function Page() {
  const { data } = useGetCampaigns();

  const campaigns = data?.data.sort((a, b) => b - a) ?? [];

  return (
    <Container p="xl">
      <Title order={1}>Campaigns</Title>
      <SimpleGrid cols={1}>
        {campaigns.map((campaignId) => (
          <CampaignCard key={campaignId} campaignId={campaignId} />
        ))}
      </SimpleGrid>
    </Container>
  );
}
