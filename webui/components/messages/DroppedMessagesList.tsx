"use client";

import {MessagesList} from "@/components/messages/MessagesList";
import {useGetDroppedMessages} from "@/lib/byzzbench-client";
import React from "react";

export const DroppedMessagesList = () => {
    const {data} = useGetDroppedMessages({query: {retry: true}});

    return <MessagesList messageIds={data?.data ?? []}/>;
};
