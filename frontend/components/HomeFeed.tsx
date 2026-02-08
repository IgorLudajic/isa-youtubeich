import { getHomeFeed } from "@/lib/videofeed";
import VideoGrid from "./VideoGrid";
import PopularVideos from "@/components/PopularVideos";

interface HomeFeedProps {
  page?: number;
  size?: number;
}

export default async function HomeFeed({ page = 0, size = 10 }: HomeFeedProps) {
  const feed = await getHomeFeed(page, size);

  return (
    <div className="space-y-6">
        <PopularVideos />
        <h2 className="text-2xl font-bold mb-4">Najnoviji snimci</h2>
        <VideoGrid
            videos={feed.content}
            currentPage={feed.number}
            totalPages={feed.totalPages}
        />
    </div>
  );
}
