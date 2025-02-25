"use client";

import { MessagesList } from "@/components/Events/MessagesList";
import { useGetDroppedMessages } from "@/lib/byzzbench-client";

export const DroppedMessagesList = () => {
  const { data: droppedMessages } = useGetDroppedMessages();

  const reverseOrder = (droppedMessages?.data ?? []).sort((a, b) => b - a);

  return <MessagesList messageIds={reverseOrder} />;
};
