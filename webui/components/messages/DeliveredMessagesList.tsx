"use client";

import { MessagesList } from "@/components/messages/MessagesList";
import { useGetDeliveredMessages } from "@/lib/bftbench-client/generated";
import React from "react";

export const DeliveredMessagesList = () => {
  const { data } = useGetDeliveredMessages({ query: { retry: true } });

  return <MessagesList messageIds={data?.data ?? []} />;
};
