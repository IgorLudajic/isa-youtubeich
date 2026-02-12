import { getVideoDetails } from "@/lib/videofeed";
import { getFullUrl } from "@/lib/utils";
import { notFound } from "next/navigation";
import LikeDislikeButtons from "@/components/LikeDislikeButtons";
import CommentsList from "@/components/CommentsList";
import ViewTracker from "@/components/ViewTracker";
import VideoChat from "@/components/VideoChat";
import { getProfile } from "@/lib/auth";
import { i18n } from "@/lib/i18n";
import Link from "next/link";
import HlsVideoPlayer from "@/components/HlsVideoPlayer";
import LocalTime from "@/components/LocalTime";
import LocalDate from "@/components/LocalDate";
import { Calendar } from "@carbon/icons-react";

export default async function VideoPage({
  params,
  searchParams,
}: {
  params: Promise<{ id: string }>;
  searchParams: Promise<{ commentPage?: string }>;
}) {
  const id = parseInt((await params).id);
  if (isNaN(id)) {
    notFound();
  }

  const { commentPage } = await searchParams;
  const page = commentPage ? parseInt(commentPage) : 0;

  let video;
  try {
    video = await getVideoDetails(id);
  } catch {
    notFound();
  }

  // Generišemo ime za čat (ako je gost, dajemo mu random broj)
  const chatUsername = profile?.username || `Gost_${Math.floor(Math.random() * 1000)}`;

  return (
    <div className="min-h-screen">
      <ViewTracker videoId={id} />
      <div className="max-w-4xl mx-auto">
        <div className="aspect-video bg-black rounded-lg overflow-hidden mb-4 relative z-10 border">
          {video.isLive ? (
            <HlsVideoPlayer
              autoPlay
              className="w-full h-full"
              poster={video.thumbnailUrl}
              src={getFullUrl(`/api/videos/${id}/stream`)}
            />
          ) : (
            <video
              controls
              className="w-full h-full"
              poster={video.thumbnailUrl}
            >
              <source
                src={getFullUrl(`/api/videos/${id}/stream`)}
                type="video/mp4"
              />
              Your browser does not support the video tag.
            </video>
          )}
        </div>

        <div className="bg-background shadow-background shadow-[0_0_50px_50px] z-0">
          {video.isUpcoming && (
            <div className="flex items-center rounded-base border-[1.5px] border-teal-600 bg-teal-300/20 p-3 mb-5 shadow-[1px_1px_0px_0px] shadow-teal-600">
              {" "}
              <span className="font-[450] mr-1.5">
                Ovaj snimak će se premijerno pustiti tek u
              </span>
              <span className="text-teal-800 font-semibold flex items-center gap-1">
                <Calendar className="-mr-0.5" />
                <span>
                  <LocalTime date={video.premieresAt + "Z"} />,
                </span>
                <LocalDate date={video.premieresAt + "Z"} />
              </span>
              {/*<Button*/}
              {/*  size="sm"*/}
              {/*  variant="noShadow"*/}
              {/*  className="ml-auto bg-teal-600/10 hover:bg-teal-600/20 text-gray-800 border-1 border-teal-600"*/}
              {/*>*/}
              {/*  Otkaži i objavi odmah*/}
              {/*</Button>*/}
            </div>
          )}

          <div className="flex items-center justify-between">
            <h1 className="text-2xl font-heading">{video.title}</h1>
            <span className="text-xl font-[600] ">
              {video.viewCount} {i18n.views(video.viewCount)}
            </span>
          </div>

          <div className="flex items-start justify-between mb-4">
            <div>
              <Link
                href={`/profiles/${video.creatorUsername}`}
                className="text-lime-700 font-mono font-[600] hover:underline"
              >
                @{video.creatorUsername}
              </Link>
              <p className="text-sm text-stone-500 mt-1">
                Objavljeno{" "}
                <span>{new Date(video.createdAt).toLocaleDateString()}</span>
              </p>
            </div>

            <div className="flex gap-2 text-sm pt-2">
              <LikeDislikeButtons
                videoId={id}
                initialLikes={video.likes}
                initialDislikes={video.dislikes}
                initialLiked={video.likedByCurrentUser}
                initialDisliked={video.dislikedByCurrentUser}
              />
            </div>
          </div>

          <div>{video.description}</div>
        </div>

        <div className="mt-12">
          <CommentsList videoId={id} page={page} />
        </div>
      </div>
    </div>
  );
}
