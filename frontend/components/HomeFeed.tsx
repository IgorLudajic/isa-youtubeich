import { getHomeFeed } from "@/lib/videofeed";
import VideoGrid from "./VideoGrid";
import PopularVideos from "@/components/PopularVideos";
import ActiveWatchParties from "./ActiveWatchParties";
import HomeFeedPagination from "./HomeFeedPagination";

interface HomeFeedProps {
  page?: number;
  size?: number;
}

export default async function HomeFeed({ page = 0, size = 10 }: HomeFeedProps) {
  const feed = await getHomeFeed(page, size);

  return (
    <div className="space-y-6">
        <ActiveWatchParties />
        <PopularVideos />
        <h2 className="text-2xl font-bold mb-4">Najnoviji snimci</h2>
        <VideoGrid videos={feed.content}>
            <HomeFeedPagination
              currentPage={feed.number}
              totalPages={feed.totalPages} />
        </VideoGrid>
    </div>
  );
}
