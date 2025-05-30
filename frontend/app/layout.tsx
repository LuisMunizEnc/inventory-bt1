import type { Metadata } from "next";
import '@/styles/globals.css';

export const metadata: Metadata = {
  title: "Inventory: Breakable toy 1",
  description: "Dashboard",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en" suppressHydrationWarning>
      <body>
        {children}
      </body>
    </html>
  );
}