import { SaveScheduleModal } from "./SaveScheduleModal";
import { ScheduleDetailsModal } from "./ScheduleDetailsModal";

const contextModals = {
  saveSchedule: SaveScheduleModal,
  scheduleDetails: ScheduleDetailsModal,
};

declare module "@mantine/modals" {
  export interface MantineModalsOverride {
    modals: typeof contextModals;
  }
}

export { contextModals };
