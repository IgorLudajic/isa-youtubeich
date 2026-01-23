import { getHomeFeed } from "@/lib/videofeed";
import VideoCard from "./VideoCard";
import HomeFeedPagination from "./HomeFeedPagination";

interface HomeFeedProps {
  page?: number;
  size?: number;
}

export default async function HomeFeed({ page = 0, size = 10 }: HomeFeedProps) {
  const feed = await getHomeFeed(page, size);

  return (
    <div className="space-y-6">
      <div className="grid grid-cols-1 gap-6 homefeed-grid md:mx-0! md:flex flex-wrap bg-background shadow-background shadow-[0_0_50px_70px] rounded-full">
        {feed.content.map((video) => (
          <VideoCard key={video.Id} video={video} />
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
      <HomeFeedPagination
        currentPage={feed.number}
        totalPages={feed.totalPages}
      />
    </div>
  );
}
