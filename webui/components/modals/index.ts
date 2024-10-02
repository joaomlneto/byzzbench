import { ChangeScenarioModal } from "@/components/modals/ChangeScenarioModal";
import { SaveScheduleModal } from "./SaveScheduleModal";
import { ScheduleDetailsModal } from "./ScheduleDetailsModal";

const contextModals = {
  changeScenario: ChangeScenarioModal,
  saveSchedule: SaveScheduleModal,
  scheduleDetails: ScheduleDetailsModal,
};

declare module "@mantine/modals" {
  export interface MantineModalsOverride {
    modals: typeof contextModals;
  }
}

export { contextModals };
