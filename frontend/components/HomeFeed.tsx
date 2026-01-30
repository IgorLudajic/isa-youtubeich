import { getHomeFeed } from "@/lib/videofeed";
import VideoGrid from "./VideoGrid";

interface HomeFeedProps {
  page?: number;
  size?: number;
}

export default async function HomeFeed({ page = 0, size = 10 }: HomeFeedProps) {
  const feed = await getHomeFeed(page, size);

  return (
    <VideoGrid
      videos={feed.content}
      currentPage={feed.number}
      totalPages={feed.totalPages}
    />
  );
}
