"use client";

import type { CommitLog } from "@/lib/byzzbench-client";
import {
  useGetScenarioCommits,
  useGetScenarioFaultyReplicaIds,
} from "@/lib/byzzbench-client/generated/hooks";
import {
  Card,
  Divider,
  Group,
  Loader,
  Pagination,
  rem,
  ScrollArea,
  Stack,
  Text,
  Tooltip,
} from "@mantine/core";
import React, { useMemo, useState } from "react";

export type ScenarioCommitLogSummaryProps = {
  scenarioId: number;
};

// Simple deterministic color hash: returns an HSL string based on input
function hashColor(input: string) {
  let hash = 0;
  for (let i = 0; i < input.length; i++) {
    hash = (hash * 31 + input.charCodeAt(i)) >>> 0;
  }
  const hue = hash % 360;
  const sat = 55 + (hash % 30); // 55-85
  const light = 45 + (hash % 20); // 45-65
  return `hsl(${hue} ${sat}% ${light}%)`;
}

function Square({
  title,
  color,
  empty = false,
}: Readonly<{
  title: string;
  color: string;
  empty?: boolean;
}>) {
  return (
    <Tooltip label={title} withArrow>
      <div
        style={{
          width: rem(12),
          height: rem(12),
          background: empty ? "transparent" : color,
          borderRadius: 2,
          border: empty
            ? "1px solid rgba(0,0,0,0.3)"
            : "1px solid rgba(0,0,0,0.1)",
          boxSizing: "border-box",
        }}
      />
    </Tooltip>
  );
}

// Render a row of colored squares for a replica commit log
function ReplicaCommitRow({
  scenarioId,
  replicaId,
  commitLog,
  windowStart,
  windowEnd,
}: Readonly<{
  scenarioId: number;
  replicaId: string;
  commitLog: CommitLog | undefined;
  windowStart: number;
  windowEnd: number; // exclusive
}>) {
  // Attempt to extract a value for a given absolute index from various possible shapes
  const extractValueAtIndex = (log: CommitLog | undefined, index: number): unknown => {
    if (!log) return undefined;
    const anyLog: any = log as any;
    const lowest = anyLog?.lowestSequenceNumber ?? 0;

    // 1) entries map keyed by absolute index
    if (anyLog && anyLog.entries && typeof anyLog.entries === "object") {
      if (index in anyLog.entries) return anyLog.entries[index];
    }

    // 2) values array aligned with lowestSequenceNumber
    if (Array.isArray(anyLog?.values)) {
      const rel = index - lowest;
      if (rel >= 0 && rel < anyLog.values.length) return anyLog.values[rel];
    }

    // 3) commits array of objects possibly carrying index/sequenceNumber and value
    if (Array.isArray(anyLog?.commits)) {
      const found = anyLog.commits.find(
        (c: any) => c?.index === index || c?.sequenceNumber === index,
      );
      if (found) {
        return (
          found.value ?? found.val ?? found.data ?? found.payload ?? found.entry
        );
      }
    }

    // 4) items/log arrays aligned with lowest
    if (Array.isArray(anyLog?.items)) {
      const rel = index - lowest;
      if (rel >= 0 && rel < anyLog.items.length) return anyLog.items[rel];
    }
    if (Array.isArray(anyLog?.log)) {
      const rel = index - lowest;
      if (rel >= 0 && rel < anyLog.log.length) return anyLog.log[rel];
    }

    return undefined;
  };

  const formatValue = (val: unknown): string => {
    try {
      if (val === null || val === undefined) return "";
      if (typeof val === "string") return val;
      if (typeof val === "number" || typeof val === "boolean") return String(val);
      return JSON.stringify(val);
    } catch {
      return String(val);
    }
  };
  const length = commitLog?.length ?? 0;
  const lowest = commitLog?.lowestSequenceNumber ?? 0;
  const isEmpty = commitLog?.empty || length === 0;
  const highest =
    commitLog?.highestSequenceNumber ?? (length ? lowest + length - 1 : -1);

  const squares = [] as React.ReactNode[];
  for (let idx = windowStart; idx < windowEnd; idx++) {
    const present = !isEmpty && idx >= lowest && idx <= highest;
    // Determine color based on the committed value (same value => same color across replicas/indices)
    let color = "hsl(0 0% 75%)"; // neutral fallback for present-but-unknown values
    let extracted: unknown = undefined;
    if (present) {
      extracted = extractValueAtIndex(commitLog, idx);
      if (extracted !== undefined) {
        const normalized = formatValue(extracted);
        color = hashColor("v:" + normalized);
      }
    }
    let title = present
      ? `Replica ${replicaId} • index ${idx}`
      : `Replica ${replicaId} • index ${idx} (missing)`;
    if (present) {
      const v = extracted;
      if (v !== undefined) {
        const s = formatValue(v);
        const trimmed = s.length > 200 ? s.slice(0, 197) + "…" : s;
        title += ` • value: ${trimmed}`;
      }
    }
    squares.push(
      <Square key={idx} title={title} color={color} empty={!present} />,
    );
  }

  return (
    <Group gap="xs" wrap="nowrap" align="flex-start">
      <Text size="sm" fw={500} style={{ width: rem(120) }}>
        {replicaId}
      </Text>
      <Group gap={4} wrap="wrap" style={{ maxWidth: "100%" }}>
        {squares}
      </Group>
    </Group>
  );
}

export const ScenarioCommitLogSummary = ({
  scenarioId,
}: ScenarioCommitLogSummaryProps) => {
  const faultyQuery = useGetScenarioFaultyReplicaIds(scenarioId);
  const commitsQuery = useGetScenarioCommits(scenarioId);
  const [page, setPage] = useState(1);
  const pageSize = 25;

  const content = useMemo(() => {
    if (faultyQuery.isLoading || commitsQuery.isLoading) {
      return (
        <Group gap="sm">
          <Loader size="sm" />
          <Text size="sm">Loading commit logs…</Text>
        </Group>
      );
    }

    if (faultyQuery.error || commitsQuery.error) {
      return (
        <Text size="sm" c="red">
          Failed to load replica information
        </Text>
      );
    }

    const faulty = new Set(faultyQuery.data?.data ?? []);
    const commitsMap = (commitsQuery.data?.data ?? {}) as Record<string, CommitLog>;
    const pairs = Object.entries(commitsMap).map(([id, log]) => ({ id, log }));

    const nonFaultyPairs = pairs.filter((p) => !faulty.has(p.id));

    if (nonFaultyPairs.length === 0) {
      return (
        <Text size="sm" c="dimmed">
          No non-faulty replicas to display
        </Text>
      );
    }

    // Determine global highest index across non-faulty logs
    let highestIndex = -1;
    for (const { log } of nonFaultyPairs) {
      const lowest = log?.lowestSequenceNumber ?? 0;
      const length = log?.length ?? 0;
      const highest =
        log?.highestSequenceNumber ?? (length ? lowest + length - 1 : -1);
      if (highest > highestIndex) highestIndex = highest;
    }

    const totalCount = highestIndex + 1; // indices 0..highest inclusive
    const totalPages = Math.max(1, Math.ceil(totalCount / pageSize));

    // Clamp page if data changes
    const currentPage = Math.min(page, totalPages);
    const windowStart = (currentPage - 1) * pageSize;
    const windowEnd = Math.min(windowStart + pageSize, totalCount);

    return (
      <Stack gap="xs">
        {totalCount > pageSize && (
          <Group justify="space-between" align="center">
            <Text size="sm" c="dimmed">
              Showing commits {windowStart}–{windowEnd - 1} of {totalCount}
            </Text>
            <Pagination
              value={currentPage}
              onChange={setPage}
              total={totalPages}
              size="sm"
            />
          </Group>
        )}
        {nonFaultyPairs.map(({ id, log }) => (
          <ReplicaCommitRow
            key={id}
            scenarioId={scenarioId}
            replicaId={id}
            commitLog={log}
            windowStart={windowStart}
            windowEnd={windowEnd}
          />
        ))}
        {totalCount > pageSize && (
          <Group justify="flex-end" align="center">
            <Pagination
              value={currentPage}
              onChange={setPage}
              total={totalPages}
              size="sm"
            />
          </Group>
        )}
      </Stack>
    );
  }, [
    faultyQuery.isLoading,
    faultyQuery.error,
    commitsQuery.isLoading,
    commitsQuery.error,
    scenarioId,
    page,
    faultyQuery.data?.data,
    commitsQuery.data?.data,
  ]);

  return (
    <Card withBorder shadow="xs" p="sm">
      <Stack gap="xs">
        <Text fw={600}>Commit log summary (non-faulty replicas)</Text>
        <Divider my={0} />
        <ScrollArea.Autosize mah={300} type="auto" offsetScrollbars>
          <Stack gap="sm" p="xs">
            {content}
          </Stack>
        </ScrollArea.Autosize>
      </Stack>
    </Card>
  );
};

export default ScenarioCommitLogSummary;
