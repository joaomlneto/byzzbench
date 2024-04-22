"use client";

import { MessagesList } from "@/components/messages/MessagesList";
import { useGetMessagesDelivered } from "@/lib/bftbench-client";
import React from "react";

export const DeliveredMessagesList = () => {
  const { data } = useGetMessagesDelivered({
    query: { refetchInterval: 1000 },
  });

  return <MessagesList messages={data?.data ?? {}} />;
};
