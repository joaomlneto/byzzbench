import { CampaignDetails } from "@/components/CampaignDetails";
import { Container, Title } from "@mantine/core";
import React from "react";

export default async function Page({
  params,
}: {
  params: Promise<{ slug: string }>;
}) {
  const { slug } = await params;

  if (!slug) {
    return "loading data";
  }

  return (
    <Container p="xl">
      <Title order={1}>Campaign {slug}</Title>
      <CampaignDetails campaignId={Number(slug)} />
    </Container>
  );
}
