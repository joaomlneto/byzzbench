"use client";

import { useGetScheduler } from "@/lib/byzzbench-client";
import { JsonInput, JsonInputProps } from "@mantine/core";

export type SchedulerStateProps = JsonInputProps & {
  schedulerId: string;
};

export const SchedulerState = ({
  schedulerId,
  ...otherProps
}: SchedulerStateProps) => {
  const { data } = useGetScheduler(schedulerId);
  // hide config from state view - its available elsewhere
  const state = { ...data?.data, config: undefined };

  return (
    <JsonInput
      value={JSON.stringify(state, null, 2)}
      readOnly
      autosize
      {...otherProps}
    />
  );
};
