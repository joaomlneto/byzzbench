import { contextModals } from "@/components/modals";
import { MainLayout } from "@/layouts/MainLayout";
import { TanstackQueryClientProvider } from "@/lib/TanstackQueryClientProvider";
import { ColorSchemeScript, MantineProvider } from "@mantine/core";
import { ModalsProvider } from "@mantine/modals";
import { Notifications } from "@mantine/notifications";
import type { Metadata } from "next";
import "@mantine/core/styles.css";
import "@mantine/charts/styles.css";
import "@mantine/dates/styles.css";
import "@mantine/notifications/styles.css";
import "@mantine/nprogress/styles.css";
import React from "react";

export const metadata: Metadata = {
  title: "BFT Bench",
  description: "Generated by create next app",
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en">
      <head>
        <ColorSchemeScript />
      </head>
      <body>
        <MantineProvider>
          <Notifications />
          <ModalsProvider
            modals={contextModals}
            modalProps={{ centered: true }}
          >
            <TanstackQueryClientProvider>
              <MainLayout>{children}</MainLayout>
            </TanstackQueryClientProvider>
          </ModalsProvider>
        </MantineProvider>
      </body>
    </html>
  );
}
