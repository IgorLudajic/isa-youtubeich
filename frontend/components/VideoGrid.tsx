import VideoCard from "./VideoCard";
import HomeFeedPagination from "./HomeFeedPagination";

interface VideoGridProps {
  videos: any[];
  currentPage: number;
  totalPages: number;
}

export default function VideoGrid({
  videos,
  currentPage,
  totalPages,
}: VideoGridProps) {
  return (
    <div className="space-y-6">
      <div className="grid grid-cols-1 gap-6 homefeed-grid md:mx-0! md:flex flex-wrap bg-background shadow-background shadow-[0_0_50px_70px] rounded-full">
        {videos.map((video) => (
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
      <HomeFeedPagination currentPage={currentPage} totalPages={totalPages} />
    </div>
  );
}
