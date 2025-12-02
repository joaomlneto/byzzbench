import { useGetCampaignName } from "@/lib/byzzbench-client";
import { Text, TextProps } from "@mantine/core";

export type CampaignNameTextProps = TextProps & {
  campaignId: number;
};

export const CampaignNameText = ({
  campaignId,
  ...otherProps
}: CampaignNameTextProps) => {
  const { data } = useGetCampaignName(campaignId);
  return <Text {...otherProps}>{data?.data}</Text>;
};
