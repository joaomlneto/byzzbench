"use client";

import {MessagesList} from "@/components/messages/MessagesList";
import {useGetQueuedMessages} from "@/lib/byzzbench-client";
import React from "react";

export const QueuedMessagesList = () => {
    const {data} = useGetQueuedMessages({query: {retry: true}});

    return <MessagesList messageIds={data?.data ?? []}/>;
};
