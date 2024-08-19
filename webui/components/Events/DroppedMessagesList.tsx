"use client";

import { MessagesList } from "@/components/Events/MessagesList";
import { useGetDroppedMessages } from "@/lib/byzzbench-client";

export const DroppedMessagesList = () => {
  const { data: droppedMessages } = useGetDroppedMessages();
  return <MessagesList messageIds={droppedMessages?.data ?? []} />;
};
