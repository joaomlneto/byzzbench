"use client";

import { Schedule } from "@/lib/byzzbench-client";
import { create } from "zustand";
import { persist } from "zustand/middleware";

export type SavedSchedule = {
  id: string;
  schedule: Schedule;
};

export type SchedulesStoreState = {
  schedules: SavedSchedule[];
  addSchedule: (id: string, schedule: Schedule) => void;
  removeSchedule: (id: string) => void;
  reset: () => void;
};

const opts = {
  name: "byzzbench-schedules",
  //partialize: (state) => ({ schedules: state.schedules }),
};

export const useSavedSchedulesStore = create<SchedulesStoreState>()(
  persist(
    (set, get) => ({
      schedules: [] satisfies SavedSchedule[],
      addSchedule: (id: string, schedule: Schedule) =>
        set((state) => ({ schedules: [...get().schedules, { id, schedule }] })),
      removeSchedule: (id: string) =>
        set((state) => ({
          schedules: state.schedules.filter((s) => s.id !== id),
        })),
      reset: () => set({ schedules: [] }),
    }),
    opts,
  ),
);
