import { Anchor, AnchorProps } from "@mantine/core";
import Link, { LinkProps } from "next/link";
import { PropsWithChildren } from "react";

export type BaseAnchorProps = PropsWithChildren<AnchorProps & LinkProps>;

export const BaseAnchor = ({ children, ...props }: BaseAnchorProps) => {
  return (
    <Anchor component={Link} {...props}>
      {children}
    </Anchor>
  );
};
