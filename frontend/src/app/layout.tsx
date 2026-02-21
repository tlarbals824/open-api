export const metadata = {
  title: "Backend API Client",
  description: "Next.js frontend using generated API client",
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="ko">
      <body>{children}</body>
    </html>
  );
}
