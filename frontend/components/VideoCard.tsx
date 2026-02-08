import Link from "next/link";
import ImageCard from "@/components/ui/image-card";
import { VideoHomeDto } from "@/lib/videofeed";
import { i18n } from "@/lib/i18n";
import LocalDate from "@/components/LocalDate";
import LocalTime from "@/components/LocalTime";
import { DotMark } from "@carbon/icons-react";

const API_BASE_URL = "http://localhost:8080/api";

interface VideoCardProps {
  video: VideoHomeDto;
}

export default function VideoCard({ video }: VideoCardProps) {
  const thumbnailUrl = `${API_BASE_URL}/videos/${video.Id}/thumbnail`;
  const getVideoStatus = () => {
    if (video.isLive) {
      return (
        <span className="text-red-600 font-bold bg-background rounded px-1.5 border-2 border-black flex">
          <span className="animate-pulse flex items-center">
            <DotMark className="-ml-1" /> Uživo
          </span>
        </span>
      );
    } else if (video.isUpcoming) {
      console.log(video.premieresAt);
      const date = new Date(
        video.premieresAt.includes("Z")
          ? video.premieresAt
          : video.premieresAt + "Z",
      );
      return (
        <span className="text-blue-600 flex items-center gap-x-1 bg-background rounded px-1.5 border-2 border-black">
          <span className="">Premijera u</span>
          <span className="">
            <LocalTime date={date} />
          </span>
        </span>
      );
    }
    return <LocalDate date={video.createdAt} />;
  };
  return (
    <Link href={`/video/${video.Id}`}>
      <ImageCard imageUrl={thumbnailUrl} className="cursor-pointer">
        <h3 className="text-lg line-clamp-2 font-heading leading-none pb-0.25">
          {video.title}
        </h3>

        <div className="mt-1 text-sm flex items-center flex-wrap">
          <span className="font-mono grow">@{video.creatorUsername}</span>
          <span className="text-gray-600 truncate">{getVideoStatus()}</span>
        </div>

        <div className="flex justify-between text-sm text-muted-foreground mt-2">
          <span>
            {video.viewCount} {i18n.views(video.viewCount)}
          </span>
          <span>
            {video.likes} {i18n.likes(video.likes)}
          </span>
        </div>
      </ImageCard>
    </Link>
  );
}
