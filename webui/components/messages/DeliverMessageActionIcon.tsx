"use client";

import {useDeliverMessage} from "@/lib/byzzbench-client/generated";
import {ActionIcon} from "@mantine/core";
import {showNotification} from "@mantine/notifications";
import {IconSend} from "@tabler/icons-react";
import {useQueryClient} from "@tanstack/react-query";

type DeliverMessageActionIconProps = {
    messageId: number;
};

export const DeliverMessageActionIcon = ({
                                             messageId,
                                         }: DeliverMessageActionIconProps) => {
    const queryClient = useQueryClient();
    const {mutate} = useDeliverMessage(messageId);

    return (
        <ActionIcon
            onClick={(e) => {
                e.preventDefault();
                mutate(null as never, {
                    onSuccess: () => {
                        showNotification({
                            message: `Message ${messageId} delivered`,
                        });
                    },
                    onError: () => {
                        showNotification({
                            message: "Failed to deliver message",
                            color: "red",
                        });
                    },
                    onSettled: async () => {
                        await queryClient.invalidateQueries();
                    },
                });
            }}
        >
            <IconSend/>
        </ActionIcon>
    );
};
