"use client";

import { useGetScenarioPredicates } from "@/lib/byzzbench-client";
import { Badge, Group, Tooltip } from "@mantine/core";
import React from "react";

export type PredicateListProps = {
  scenarioId: number;
};

export const PredicateList = ({ scenarioId }: PredicateListProps) => {
  const { data } = useGetScenarioPredicates(scenarioId);

  return (
    <Group p="sm">
      {data?.data.map((predicate) => (
        <Tooltip key={predicate.id} label={predicate.explanation}>
          <Badge color={predicate.satisfied ? "green" : "red"}>
            {predicate.id}
          </Badge>
        </Tooltip>
      ))}
    </Group>
  );
};
