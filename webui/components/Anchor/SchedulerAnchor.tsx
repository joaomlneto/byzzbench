"use client";

import { BaseAnchor } from "@/components/Anchor";
import { AnchorProps } from "@mantine/core";
import { PropsWithChildren } from "react";

export type SchedulerAnchorProps = PropsWithChildren<
  AnchorProps & {
    schedulerId: string;
  }
>;

export const SchedulerAnchor = ({
  children,
  ...props
}: SchedulerAnchorProps) => {
  return (
    <BaseAnchor href={`/schedulers/${props.schedulerId}`} {...props}>
      {children}
    </BaseAnchor>
  );
};
