import { getPublicProfile } from "@/lib/profile";
import { notFound } from "next/navigation";
import VideoCard from "@/components/VideoCard";
import HomeFeedPagination from "@/components/HomeFeedPagination";

export default async function ProfilePage({
  params,
  searchParams,
}: {
  params: Promise<{ username: string }>;
  searchParams: Promise<{ page?: string }>;
}) {
  const { username } = await params;
  const { page: pageParam } = await searchParams;
  const page = pageParam ? parseInt(pageParam) : 0;

  let profile;
  try {
    profile = await getPublicProfile(username, page, 10);
  } catch {
    notFound();
  }

  return (
    <div className="container mx-auto p-4 md:p-8 space-y-12">
      <section className="space-y-2">
        <h1 className="text-4xl font-heading">@{profile.username}</h1>
        <div className="flex flex-col text-lg font-medium">
          <span>
            {profile.firstName} {profile.lastName}
          </span>
          <span className="text-stone-600">{profile.email}</span>
          <span className="text-stone-500 text-sm mt-1">
            Member since {new Date(profile.createdAt).toLocaleDateString()}
          </span>
        </div>
      </section>

      <div className="relative">
        <div className="absolute inset-0 flex items-center" aria-hidden="true">
          <div className="w-[100vw] relative left-1/2 right-1/2 -ml-[50vw] -mr-[50vw] border-t-4 border-black"></div>
        </div>
      </div>

      <div className="space-y-8 pt-4">
        <h2 className="text-2xl font-heading">Videos</h2>

        {profile.videos.content.length > 0 ? (
          <>
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 2xl:grid-cols-5 gap-6">
              {profile.videos.content.map((video) => (
                <VideoCard key={video.Id} video={video} />
              ))}
            </div>

            <HomeFeedPagination
              currentPage={profile.videos.number}
              totalPages={profile.videos.totalPages}
            />
          </>
        ) : (
          <p className="text-stone-500 italic">
            This user has no published videos.
          </p>
        )}
      </div>
    </div>
  );
}
