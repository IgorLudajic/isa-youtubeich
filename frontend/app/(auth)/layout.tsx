import Footer from "@/components/Footer";

export default function MainLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <>
      <div className="min-h-[90dvh] bg-grid flex flex-col px-2">{children}</div>
      <Footer />
    </>
  );
}
