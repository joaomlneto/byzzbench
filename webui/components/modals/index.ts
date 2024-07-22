import { SaveScheduleModal } from "./SaveScheduleModal";

const contextModals = {
  saveSchedule: SaveScheduleModal,
};

declare module "@mantine/modals" {
  export interface MantineModalsOverride {
    modals: typeof contextModals;
  }
}

export { contextModals };
