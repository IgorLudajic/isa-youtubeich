import { getPublicProfile, UserPublicProfileDto } from "@/lib/profile";
import { notFound } from "next/navigation";
import ProfileAvatar from "@/components/ProfileAvatar";
import VideoGrid from "@/components/VideoGrid";

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

  const adaptProfile = (profile: UserPublicProfileDto) => ({
    name: profile.firstName,
    surname: profile.lastName,
    avatarUrl: undefined,
  });

  return (
    <div className="container mx-auto">
      <section className="space-y-2 p-4 rounded-base border-border border-2 bg-background">
        <div className="flex flex-col text-lg font-medium">
          <div className="flex gap-4 items-center mb-2">
            <ProfileAvatar
              className="size-13"
              profile={adaptProfile(profile)}
            />
            <div className="flex flex-col">
              <span className="text-xl font-heading">
                {profile.firstName} {profile.lastName}
              </span>
              <span className="font-mono">@{profile.username}</span>
            </div>
          </div>

          <span className="text-stone-600 font-mono">{profile.email}</span>
          <span className="text-stone-500 text-sm mt-1">
            Član od {new Date(profile.createdAt).toLocaleDateString()}
          </span>
        </div>
      </section>

      <div className="mt-8 py-4 bg-background shadow-background shadow-[0_20px_50px_50px] rounded-4xl">
        <h2 className="text-lg ml-2 font-heading mb-4 select-none">
          Video snimci
        </h2>

        {profile.videos.content.length > 0 ? (
          <VideoGrid
            videos={profile.videos.content}
            currentPage={profile.videos.number}
            totalPages={profile.videos.totalPages}
          />
        ) : (
          <p className="text-stone-500 text-sm text-center select-none">
            Korisnik nije objavio nijedan snimak.
          </p>
        )}
      </div>
    </div>
  );
}
