"use client";

import { MessagesList } from "@/components/messages/MessagesList";
import { useGetMessagesCaptive } from "@/lib/bftbench-client";
import React from "react";

export const QueuedMessagesList = () => {
  const { data } = useGetMessagesCaptive({ query: { refetchInterval: 1000 } });

  return <MessagesList messages={data?.data ?? {}} />;
};
