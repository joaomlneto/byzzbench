"use client";

import { useGetSchedulers } from "@/lib/byzzbench-client";
import { Select, SelectProps } from "@mantine/core";
import React, { useEffect, useMemo } from "react";

export type SchedulerSelectProps = Omit<SelectProps, "data">;

export const SchedulerSelect = ({ ...otherProps }: SchedulerSelectProps) => {
  const { data } = useGetSchedulers();

  const schedulers = useMemo(() => data?.data ?? [], [data?.data]);

  useEffect(() => {
    if (schedulers.length > 0 && !otherProps.value) {
      // @ts-expect-error
      otherProps.onChange?.(schedulers[0], undefined);
    }
  }, [schedulers, otherProps]);

  return <Select {...otherProps} data={schedulers} />;
};
