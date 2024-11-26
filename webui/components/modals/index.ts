import { ChangeScenarioModal } from "@/components/modals/ChangeScenarioModal";
import { ShowConfigModal } from "@/components/modals/ShowConfigModal";
import { SaveScheduleModal } from "./SaveScheduleModal";
import { ScheduleDetailsModal } from "./ScheduleDetailsModal";

const contextModals = {
  changeScenario: ChangeScenarioModal,
  saveSchedule: SaveScheduleModal,
  scheduleDetails: ScheduleDetailsModal,
  showConfig: ShowConfigModal,
};

declare module "@mantine/modals" {
  export interface MantineModalsOverride {
    modals: typeof contextModals;
  }
}

export { contextModals };
