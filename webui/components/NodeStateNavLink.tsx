import { NavLink, NavLinkProps, Text, Tooltip } from "@mantine/core";
import {
  Icon123,
  IconCircleCheck,
  IconCircleOff,
  IconCylinder,
  IconFileDigit,
  IconList,
  IconX,
} from "@tabler/icons-react";
import React from "react";

export type NodeStateNavLinkProps = NavLinkProps & {
  data?: any;
};

export const NodeStateNavLink = ({ data, label }: NodeStateNavLinkProps) => {
  if (data === null || data === undefined) {
    return (
      <NavLink
        label={label}
        leftSection={<IconX size={16} />}
        //description={typeof data}
        rightSection={data === null ? "null" : "undefined"}
      />
    );
  }

  if (typeof data === "boolean") {
    return (
      <NavLink
        label={label}
        leftSection={<IconFileDigit size={16} />}
        description={typeof data}
        rightSection={
          data ? (
            <Tooltip label="true">
              <IconCircleCheck size={16} color="green" />
            </Tooltip>
          ) : (
            <Tooltip label="false">
              <IconCircleOff size={16} color="red" />
            </Tooltip>
          )
        }
      />
    );
  }

  if (Array.isArray(data)) {
    return (
      <NavLink
        leftSection={<IconList size={16} />}
        label={label}
        description={<Text size="sm">Array ({data.length})</Text>}
      >
        {data.map((value, index) => (
          <NodeStateNavLink key={index} label={index} data={value} />
        ))}
      </NavLink>
    );
  }

  if (typeof data !== "object") {
    return (
      <NavLink
        leftSection={
          typeof data === "string" ? (
            <IconFileDigit size={16} />
          ) : typeof data === "number" ? (
            <Icon123 size={16} />
          ) : undefined
        }
        label={data["__className__"] ?? label ?? "(unknown class)"}
        description={typeof data}
        rightSection={
          <Text
            size="sm"
            fw={700}
            style={{ overflowWrap: "anywhere", maxWidth: "150px" }}
          >
            {data ?? "null"}
          </Text>
        }
      />
    );
  }

  return (
    <NavLink
      label={label ?? data["__className__"] ?? "(unknown class)"}
      description={`${(data["__className__"] ?? "").replace(/.*\./, "")} (${Object.keys(data).length})`}
      leftSection={<IconCylinder size={16} />}
    >
      {Object.entries(data ?? {})
        .filter(([key]) => key !== "__className__")
        .sort(([key1], [key2]) => key1.localeCompare(key2))
        .map(([key, value]) => {
          return <NodeStateNavLink key={key} label={key} data={value} />;
        })}
    </NavLink>
  );
};
