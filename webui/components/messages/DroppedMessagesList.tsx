"use client";

import { MessagesList } from "@/components/messages/MessagesList";
import { useGetMessagesDropped } from "@/lib/bftbench-client";
import React from "react";

export const DroppedMessagesList = () => {
  const { data } = useGetMessagesDropped({ query: { refetchInterval: 1000 } });

  return <MessagesList messages={data?.data ?? {}} />;
};
