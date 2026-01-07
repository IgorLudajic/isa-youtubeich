import Link from "next/link";
import ImageCard from "@/components/ui/image-card";
import { VideoHomeDto } from "@/lib/videofeed";
import { getFullUrl } from "@/lib/utils";
import { i18n } from "@/lib/i18n";

interface VideoCardProps {
  video: VideoHomeDto;
}

export default function VideoCard({ video }: VideoCardProps) {
  return (
    <Link href={`/video/${video.Id}`}>
      <ImageCard
        imageUrl={getFullUrl(video.thumbnailUrl)}
        className="cursor-pointer"
      >
        <h3 className="text-lg line-clamp-2 font-heading leading-none">
          {video.title}
        </h3>
        <p className="text-sm text-muted-foreground">{video.creatorUsername}</p>
        <div className="flex justify-between text-sm text-muted-foreground mt-2">
          <span>
            {video.viewCount} {i18n.views(video.viewCount)}
          </span>
          <span>
            {video.likes} {i18n.likes(video.likes)}
          </span>
        </div>
        <p className="text-xs text-muted-foreground mt-1">
          {new Date(video.createdAt).toLocaleDateString()}
        </p>
      </ImageCard>
    </Link>
  );
}
