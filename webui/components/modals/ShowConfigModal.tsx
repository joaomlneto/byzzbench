"use client";

import { useGetConfig } from "@/lib/byzzbench-client";
import { JsonInput, Stack } from "@mantine/core";
import { type ContextModalProps } from "@mantine/modals";
import { useQueryClient } from "@tanstack/react-query";
import React from "react";

export function ShowConfigModal({ innerProps }: ContextModalProps<{}>) {
  const queryClient = useQueryClient();
  const { data } = useGetConfig();

  return (
    <Stack gap="sm">
      <JsonInput
        label="Params"
        placeholder="Params"
        minRows={10}
        maxRows={30}
        autosize
        formatOnBlur
        readOnly
        value={JSON.stringify(data?.data, null, 2)}
      />
    </Stack>
  );
}
