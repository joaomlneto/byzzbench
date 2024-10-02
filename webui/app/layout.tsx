import { contextModals } from "@/components/modals";
import { MainLayout } from "@/layouts/MainLayout";
import { TanstackQueryClientProvider } from "@/lib/TanstackQueryClientProvider";
import { ColorSchemeScript, MantineProvider } from "@mantine/core";
import { ModalsProvider } from "@mantine/modals";
import { Notifications } from "@mantine/notifications";
import "@mantine/core/styles.css";
import "@mantine/charts/styles.css";
import "@mantine/dates/styles.css";
import "@mantine/notifications/styles.css";
import "@mantine/nprogress/styles.css";
import React from "react";

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
        <TanstackQueryClientProvider>
          <MantineProvider>
            <Notifications />
            <ModalsProvider
              modals={contextModals}
              modalProps={{ centered: true }}
            >
              <MainLayout>{children}</MainLayout>
            </ModalsProvider>
          </MantineProvider>
        </TanstackQueryClientProvider>
      </body>
    </html>
  );
}
