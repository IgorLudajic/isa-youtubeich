import HomeFeed from "@/components/HomeFeed";

export default async function Page({
  searchParams,
}: {
  searchParams: Promise<{ page?: string }>;
}) {
  const { page } = await searchParams;
  const currentPage = page ? parseInt(page) : 0;

  return (
    <div className="max-w-screen-2xl mx-auto">
      <HomeFeed page={currentPage} />
    </div>
  );
}
