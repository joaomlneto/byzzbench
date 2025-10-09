"use client";

import { useGetScenarioPredicates } from "@/lib/byzzbench-client";
import { Badge, Group } from "@mantine/core";
import React, { useMemo } from "react";

export type PredicateListProps = {
  scenarioId: number;
};

export const PredicateList = ({ scenarioId }: PredicateListProps) => {
  const predicatesQuery = useGetScenarioPredicates(scenarioId);

  const predicates = useMemo(
    () => predicatesQuery?.data?.data ?? {},
    [predicatesQuery?.data?.data],
  );

  return (
    <Group p="sm">
      {Object.entries(predicates).map(([name, isSatisfied]) => (
        <Badge key={name} color={isSatisfied ? "green" : "red"}>
          {name}
        </Badge>
      ))}
    </Group>
  );
};
