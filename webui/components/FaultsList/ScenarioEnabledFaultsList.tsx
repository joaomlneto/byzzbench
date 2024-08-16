import { FaultsList } from "@/components/FaultsList/FaultsList";
import { useGetEnabledNetworkFaults } from "@/lib/byzzbench-client";

export const ScenarioEnabledFaultsList = () => {
  const faultsQuery = useGetEnabledNetworkFaults();
  return <FaultsList faultIds={faultsQuery.data?.data ?? []} />;
};
