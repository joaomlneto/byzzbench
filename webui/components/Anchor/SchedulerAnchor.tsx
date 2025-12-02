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
  schedulerId,
  ...rest
}: SchedulerAnchorProps) => {
  return (
    <BaseAnchor href={`/schedulers/${schedulerId}`} {...rest}>
      {children}
    </BaseAnchor>
  );
};
