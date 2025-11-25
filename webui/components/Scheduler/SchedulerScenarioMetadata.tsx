import { useGetScenarioStrategyData } from "@/lib/byzzbench-client";
import { JsonInput, JsonInputProps } from "@mantine/core";

export type SchedulerScenarioMetadataProps = Omit<JsonInputProps, "value"> & {
  schedulerId: string;
  scenarioId: number;
};

export const SchedulerScenarioMetadata = ({
  schedulerId,
  scenarioId,
  ...otherProps
}: SchedulerScenarioMetadataProps) => {
  const { data } = useGetScenarioStrategyData(schedulerId, scenarioId);
  return (
    <JsonInput
      value={JSON.stringify(data?.data, null, 2)}
      label="Strategy Metadata"
      autosize
      maxRows={20}
      readOnly
      {...otherProps}
    />
  );
};
