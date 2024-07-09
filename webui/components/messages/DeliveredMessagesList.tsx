"use client";

import { MessagesList } from "@/components/messages/MessagesList";
import { useGetSchedule } from "@/lib/byzzbench-client";
import React from "react";

export const DeliveredMessagesList = () => {
  const { data } = useGetSchedule({ query: { retry: true } });
  return <MessagesList messageIds={data?.data ?? []} />;
};
