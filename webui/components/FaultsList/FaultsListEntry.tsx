"use client";
import {
  enableNetworkFault,
  useGetEnabledNetworkFaults,
  useGetNetworkFault,
} from "@/lib/byzzbench-client";
import { ActionIcon, Card, Group, Text, Tooltip } from "@mantine/core";
import { showNotification } from "@mantine/notifications";
import { IconLighter } from "@tabler/icons-react";
import { useQueryClient } from "@tanstack/react-query";
import React, { useMemo } from "react";

export type FaultsListEntryProps = {
  faultId: string;
  disabled?: boolean;
};

export const FaultsListEntry = ({ faultId }: FaultsListEntryProps) => {
  const queryClient = useQueryClient();
  const faultQuery = useGetNetworkFault(faultId);
  const enabledFaultsQuery = useGetEnabledNetworkFaults();

  const isEnabled = useMemo(() => {
    return enabledFaultsQuery.data?.data.includes(faultId);
  }, [enabledFaultsQuery.data?.data, faultId]);
  return (
    <Card withBorder shadow="sm" padding="xs" m={4}>
      <Group gap="xs">
        <Text>{faultQuery.data?.data.name}</Text>
        {isEnabled && (
          <Tooltip label="Trigger fault">
            <ActionIcon
              size="sm"
              onClick={async (e) => {
                e.preventDefault();
                await enableNetworkFault(faultId)
                  .then(() => {
                    showNotification({
                      message: "Fault triggered",
                    });
                  })
                  .catch(() => {
                    showNotification({
                      message: "Failed to trigger fault",
                      color: "red",
                    });
                  })
                  .finally(() => {
                    queryClient.invalidateQueries();
                  });
              }}
            >
              <IconLighter />
            </ActionIcon>
          </Tooltip>
        )}
      </Group>
    </Card>
  );
};
