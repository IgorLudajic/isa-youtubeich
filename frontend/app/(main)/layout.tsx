import Header from "@/components/Header";
import Footer from "@/components/Footer";

export default function MainLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <>
      <Header />
      <div className="bg-grid min-h-[80vh] p-4 md:p-8">{children}</div>
      <Footer />
    </>
  );
}
