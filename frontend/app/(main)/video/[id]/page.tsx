import { getVideoDetails } from "@/lib/videofeed";
import { getFullUrl } from "@/lib/utils";
import { notFound } from "next/navigation";
import LikeDislikeButtons from "@/components/LikeDislikeButtons";
import CommentsList from "@/components/CommentsList";
import { i18n } from "@/lib/i18n";

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
  } catch (error) {
    notFound();
  }

  return (
    <div className="min-h-screen bg-background bg-grid p-4 md:p-8">
      <div className="max-w-4xl mx-auto">
        <div className="aspect-video bg-black rounded-lg overflow-hidden mb-4 relative z-10">
          <video
            controls
            className="w-full h-full"
            poster={getFullUrl(video.thumbnailUrl)}
          >
            <source
              src={getFullUrl(`/api/videos/${id}/stream`)}
              type="video/mp4"
            />
            Your browser does not support the video tag.
          </video>
        </div>

        <div className="bg-background shadow-background shadow-[0_0_50px_50px] z-0">
          <div className="flex items-center justify-between">
            <h1 className="text-2xl font-heading">{video.title}</h1>
            <span className="text-xl font-[600] ">
              {video.viewCount} {i18n.views(video.viewCount)}
            </span>
          </div>

          <div className="flex items-start justify-between mb-4">
            <a
              href="#"
              className="text-lime-700 font-mono font-[600] hover:underline"
            >
              @{video.creatorUsername}
            </a>
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
          <p className="text-sm text-stone-500">
            Objavljeno{" "}
            <span>{new Date(video.createdAt).toLocaleDateString()}</span>
          </p>
        </div>

        <div className="mt-8">
          <CommentsList videoId={id} page={page} />
        </div>
      </div>
    </div>
  );
}
