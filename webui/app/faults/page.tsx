"use client";

import { MutatorsList } from "@/components/MutatorsList";
import { Container, Title } from "@mantine/core";
import React from "react";

export default function Page() {
  return (
    <Container fluid p="xl">
      <Title order={1}>Message Mutators</Title>
      <MutatorsList />
    </Container>
  );
}
