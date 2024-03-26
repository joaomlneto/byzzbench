import { Stack, Table, Text, Title } from "@mantine/core";
import React from "react";

const JsonTableEntry = ({ key, value }: { key: string; value: any }) => {
  if (typeof value === "string") {
    return <Text size="xs">{value}</Text>;
  }
  if (typeof value === "object" && Array.isArray(value)) {
    return <JsonTable data={value} />;
  }
  if (typeof value === "object" && !Array.isArray(value)) {
    return <JsonTable data={value} />;
  }
  return <Text size="xs">{JSON.stringify(value)}</Text>;
};

export const JsonTable = ({
  title,
  description,
  data,
}: {
  title?: string;
  description?: string;
  data: Record<string, any>;
}) => {
  return (
    <Stack>
      {(title || description) && (
        <Stack>
          {title && <Title order={4}>{title}</Title>}
          {description && (
            <Text c="dimmed" size="sm">
              {description}
            </Text>
          )}
        </Stack>
      )}
      <Table highlightOnHover striped style={{ border: "1px solid red;" }}>
        <tbody>
          {Object.keys(data ?? {}).map((key) => (
            <tr key={key}>
              <td style={{ whiteSpace: "nowrap" }}>{key}</td>
              <td align="right">
                <JsonTableEntry key={key} value={data[key]} />
              </td>
            </tr>
          ))}
        </tbody>
      </Table>
    </Stack>
  );
};
