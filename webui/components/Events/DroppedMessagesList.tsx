"use client";

import { MessagesList } from "@/components/Events/MessagesList";
import { useGetDroppedMessages } from "@/lib/byzzbench-client";

export type DroppedMessagesListProps = {
  scenarioId: number;
};

export const DroppedMessagesList = ({
  scenarioId,
}: DroppedMessagesListProps) => {
  const { data: droppedMessages } = useGetDroppedMessages(scenarioId);

  const reverseOrder = (droppedMessages?.data ?? []).sort((a, b) => b - a);

  return <MessagesList scenarioId={scenarioId} messageIds={reverseOrder} />;
};
