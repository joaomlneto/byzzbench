import { Switch, SwitchProps } from "@mantine/core";
import { useLocalStorage } from "@mantine/hooks";

export type ScenarioMailboxSwitchProps = SwitchProps & {};

export const ShowMailboxesSwitch = ({
  ...otherProps
}: ScenarioMailboxSwitchProps) => {
  const [showMailboxes, setShowMailboxes] = useLocalStorage<boolean>({
    key: "byzzbench/showMailboxes",
    defaultValue: false,
  });

  return (
    <Switch
      label="Show mailboxes"
      checked={showMailboxes}
      onChange={(event) => {
        setShowMailboxes(event.currentTarget.checked);
      }}
      {...otherProps}
    />
  );
};
