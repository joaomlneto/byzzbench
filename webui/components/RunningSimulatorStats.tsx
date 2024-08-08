import { useGetNumSavedSchedules } from "@/lib/byzzbench-client";

export const RunningSimulatorStats = () => {
  const x = useGetNumSavedSchedules({
    query: {
      refetchInterval: 1000,
    },
  });

  return (
    <div>
      <h1>Running Simulations</h1>
      <p>Schedules saved: {x.data?.data.length}</p>
    </div>
  );
};
