"use client";

import { useGetScenarioPredicates } from "@/lib/byzzbench-client";
import { Badge, Group } from "@mantine/core";
import React, { useMemo } from "react";

export const PredicateList = () => {
  const predicatesQuery = useGetScenarioPredicates();

  const predicates = useMemo(
    () => predicatesQuery?.data?.data ?? {},
    [predicatesQuery?.data?.data],
  );

  return (
    <Group p="sm">
      {Object.entries(predicates).map(([name, isSatisfied]) => (
        <Badge color={isSatisfied ? "green" : "red"}>{name}</Badge>
      ))}
    </Group>
  );
};
