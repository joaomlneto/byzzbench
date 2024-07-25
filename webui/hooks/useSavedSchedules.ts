"use client";

import { create } from "zustand";
import { persist } from "zustand/middleware";

export type Action = DeliverEventAction;

export type DeliverEventAction = {
  type: "DeliverEvent";
  label: string;
  event: any;
};

export type Schedule = {
  name: string;
  actions: Action[];
};

export type SchedulesStoreState = {
  schedules: Schedule[];
  addSchedule: (schedule: Schedule) => void;
  removeSchedule: (schedule: Schedule) => void;
  reset: () => void;
};

const opts = {
  name: "byzzbench-schedules",
  //partialize: (state) => ({ schedules: state.schedules }),
};

export const useSavedSchedulesStore = create<SchedulesStoreState>()(
  persist(
    (set, get) => ({
      schedules: [],
      addSchedule: (schedule: Schedule) =>
        set((state) => ({ schedules: [...get().schedules, schedule] })),
      removeSchedule: (schedule: Schedule) =>
        set((state) => ({
          schedules: state.schedules.filter((s) => s !== schedule),
        })),
      reset: () => set({ schedules: [] }),
    }),
    opts,
  ),
);
