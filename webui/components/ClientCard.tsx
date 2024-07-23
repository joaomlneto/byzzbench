import { NodeStateNavLink } from "@/components/NodeStateNavLink";
import { useGetClient } from "@/lib/byzzbench-client";
import { Container, JsonInput, Stack, Title } from "@mantine/core";
import React from "react";

export type ClientCardProps = {
  clientId: string;
};

export const ClientCard = ({ clientId }: ClientCardProps) => {
  const { data } = useGetClient(clientId);

  return (
    <Container fluid style={{ border: "1px solid black" }} p="md">
      <Stack gap="xs">
        {false && (
          <JsonInput value={JSON.stringify(data?.data, null, 2)} autosize />
        )}
        {data && (
          <NodeStateNavLink
            data={data.data}
            label={<Title order={4}>{clientId}</Title>}
            defaultOpened
            opened={true}
          />
        )}
      </Stack>
    </Container>
  );
};
