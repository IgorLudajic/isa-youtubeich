import { cookies } from "next/headers";
import { getLatestPopularity, VideoPopularityDto } from "@/lib/videofeed";
import { TrophyFilled } from "@carbon/icons-react";
import VideoCard from "@/components/VideoCard";
import { cn } from "@/lib/utils";
import ForceEtlButton from "@/components/ForceEtlButton";

export default async function PopularVideos() {
  const cookieStore = await cookies();
  const token = cookieStore.get("token")?.value;
  if (!token) {
    return null;
  }

  let popularVideos: VideoPopularityDto[] = [];
  try {
    popularVideos = await getLatestPopularity();
  } catch (error) {
    console.log(error);
    return null;
  }

  if (popularVideos.length === 0) {
    return (
      <>
        <ForceEtlButton />
      </>
    )
  }

  const colors = ["text-yellow-500", "text-gray-500/80", "text-yellow-700"];

  return (
    <div className="">
      <ForceEtlButton />

      <h2 className="text-2xl font-bold mb-4">Najpopularniji snimci danas</h2>
      <div className="grid grid-cols-1 gap-6 homefeed-grid md:mx-0! md:flex flex-wrap bg-background shadow-background shadow-[0_0_50px_70px] rounded-full">
        {popularVideos.map((video, index) => (
          <div key={video.videoId}>
            <div
              className={cn(
                "flex items-center gap-1 text-xl mb-1 justify-center",
                colors[index],
              )}
            >
              <TrophyFilled className="size-6" />
              <span>#{index + 1}</span>
            </div>
            <VideoCard video={{ Id: video.videoId, ...video }} />
          </div>
        ))}
      </div>
      <style>{`
        @media (min-width: 560px) {
          .homefeed-grid {
            grid-template-columns: repeat(2, minmax(0, 1fr));
            margin-left: auto;
            margin-right: auto;
            width: fit-content;
          }
        }
      `}</style>
    </div>
  );
}
