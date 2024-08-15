import { FaultsList } from "@/components/FaultsList/FaultsList";
import { useGetNetworkFaults } from "@/lib/byzzbench-client";

export const ScenarioFaultsList = () => {
  const faultsQuery = useGetNetworkFaults();
  return <FaultsList faultIds={faultsQuery.data?.data ?? []} />;
};
