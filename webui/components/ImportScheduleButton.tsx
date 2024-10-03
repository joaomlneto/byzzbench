import { useSavedSchedulesStore } from "@/hooks/useSavedSchedules";
import { Button, ButtonProps, FileButton } from "@mantine/core";
import { showNotification } from "@mantine/notifications";

export const ImportScheduleButton = (
  props: Omit<ButtonProps, "onClick" | "children">,
) => {
  const schedules = useSavedSchedulesStore((state) => state.schedules);
  const addSchedule = useSavedSchedulesStore((x) => x.addSchedule);
  return (
    <FileButton
      onChange={async (file) => {
        // if no file is selected, return
        if (!file) return;

        // read the file as text
        const contents = await file.text();
        const id = file.name.split(".")[0];

        // check if schedule with id already exists
        if (schedules.find((x) => x.id === id)) {
          showNotification({
            message: `Schedule with id ${id} already exists`,
            color: "red",
          });
          return;
        }

        // add the schedule to the store
        addSchedule(id, JSON.parse(contents));
        showNotification({
          message: `Imported ${file.name}`,
          color: "blue",
        });
      }}
      accept="application/json,text/json"
    >
      {({ onClick }) => (
        <Button onClick={onClick} {...props}>
          Import Schedule
        </Button>
      )}
    </FileButton>
  );
};
